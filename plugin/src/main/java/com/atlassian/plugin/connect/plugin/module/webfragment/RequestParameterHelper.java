package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Helper to create the various forms we need the parameters in. It's functions are:
 * Converts between a map of individual params in path expression form like
 * [project.id -> 10; project.key -> "foo"]
 * and a nested map of params in json style form like
 * [project -> [id -> 10; key -> "foo"]]
 */
public class RequestParameterHelper
{
    // TODO: Should find a better home
    public static final String CONTEXT_PARAMETER_KEY = "context";

    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<HashMap<String, Object>>()
            {
            };

    private final Map<String, Object> requestParams;

    public RequestParameterHelper(Map<String, Object> requestParams)
    {
        this.requestParams = requestParams;
    }

    /**
     * The request parameters in the form of nested maps
     * @return
     */
    public Map<String, Object> getParamsInNestedForm()
    {
        /*
          We process by splitting params like foo.bar.id = 10 & foo.blah = 6
          into a list of pairs like ([foo, bar, id], 10) & ([foo, blah], 6)
          then convert into a multimap like foo -> {([bar, id], 10) & ([blah], 6)}
          and then recurse
         */
        final ImmutableList.Builder<Pair<List<String>, String[]>> builder = ImmutableList.builder();
        for (Map.Entry<String, String[]> entry : getParamsInPathForm().entrySet())
        {
            builder.add(Pair.of(Arrays.asList(entry.getKey().split("\\.")), entry.getValue()));
        }

        return transformToNestedMap(builder.build());
    }

    /**
     * The request parameters in the form of individual path variables
     * @return
     */
    public Map<String, String[]> getParamsInPathForm()
    {
        return Maps.transformValues(requestParams, new Function<Object, String[]>()
        {
            @Override
            public String[] apply(@Nullable Object input)
            {
                return (String[]) input;
            }
        });
    }

    /**
     * Same as #getParamsInPathForm but the Map is type with Object as it's values
     * @return
     */
    public Map<String, Object> getParamsInPathFormAsObjectValues()
    {
        return requestParams;
    }

    private Map<String, Object> transformToNestedMap(Iterable<Pair<List<String>, String[]>> pairs)
    {
        final ImmutableMultimap.Builder<String, Pair<List<String>, String[]>> builder =
                ImmutableMultimap.<String, Pair<List<String>, String[]>>builder();
        for (Pair<List<String>, String[]> pair : pairs)
        {
            final List<String> pathComponents = pair.getLeft();
            final String[] value = pair.getRight();
            if (pathComponents.isEmpty())
            {
                continue; // TODO: Should we throw error? This would happen if they passed something like foo.bar = 10 & foo = 6
//                throw new MalformedRequestException("TODO");
            }
            final String key = pathComponents.get(0);
            final List<String> remainingPathComponents = pathComponents.subList(1, pathComponents.size());

            builder.put(key, Pair.of(remainingPathComponents, value));
        }

        return Maps.transformValues(builder.build().asMap(), new Function<Collection<Pair<List<String>, String[]>>, Object>()
        {
            @Override
            public Object apply(@Nullable Collection<Pair<List<String>, String[]>> input)
            {
                return transform(input);
            }
        });

    }

    private Object transform(Iterable<Pair<List<String>, String[]>> pairs) //throws MalformedRequestException
    {
        if (Iterables.size(pairs) == 1)
        {
            final Pair<List<String>, String[]> pair = Iterables.getFirst(pairs, null);
            final List<String> pathComponents = pair.getLeft();
            final String[] value = pair.getRight();
            if (pathComponents.isEmpty())
            {
                return isEmpty(value) ? "" : value[0]; // TODO: assuming only one value. Is this valid for us?;
            }
        }

        return transformToNestedMap(pairs);
    }


    // TODO: Looks like we have moved away from the json form again. Remove??
    private Map<String, Object> extractContext(Map<String, Object> requestParams) throws InvalidContextParameterException
    {
        if (!requestParams.containsKey(CONTEXT_PARAMETER_KEY))
        {
            return requestParams;
        }

        final String[] contextParam = (String[]) requestParams.get(CONTEXT_PARAMETER_KEY);
        if (isEmpty(contextParam))
            throw new InvalidContextParameterException("Empty context received");

        final String contextJsonStr = contextParam[0];
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Map<String, Object> contextMap = objectMapper.readValue(contextJsonStr, MAP_TYPE_REFERENCE);
            // TODO: convert to path form here

            final Map<String, Object> mutableParams = Maps.newHashMap(requestParams);
            mutableParams.remove(CONTEXT_PARAMETER_KEY);
            return ImmutableMap.<String, Object>builder().putAll(mutableParams).putAll(contextMap).build();
        }
        catch (IOException e)
        {
            throw new InvalidContextParameterException("Failed to parse context Json", e);
        }

    }


}
