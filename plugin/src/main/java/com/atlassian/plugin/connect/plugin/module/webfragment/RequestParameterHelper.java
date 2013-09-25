package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.google.common.base.Function;
import com.google.common.collect.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Helper to create the various forms we need the parameters in. It's functions are:
 * - unmarshal the context from Json
 * - TODO: !!!!! complete
 * Converts between a map of params in json style form
 * [project -> [id -> 10; key -> "foo"]]
 * and a map of individual params in path expression form
 * [project.id -> 10; project.key -> "foo"]
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
        /*
         1/ extract Json context variable. !!!!! Maybe skip this step as we likely don't need
            a/ The context will be in nested form so convert to path form
            b/ Add into request params
            c/ remove context variable
            => at this point the params will be in a consistent form where all params are path style regardless of how they were passed to the servlet
            ? hold as String -> String[] or String -> Object?

         2/ when authenticating use the nested form
         3/ when substituting use path form (Object)
         4/ when calculating non path variables use path form (String[])
         */
        this.requestParams = requestParams;
    }

    public Map<String, Object> getParamsInNestedForm()
    {
        final Map<String, String> singleValueParams = Maps.transformValues(getParamsInPathForm(), new Function<String[], String>()
        {
            @Override
            public String apply(@Nullable String[] input)
            {
                return isEmpty(input) ? "" : input[0]; // TODO: assuming only one value. Is this valid for us?;
            }
        });
        final ImmutableList.Builder<Pair<List<String>, String>> listBuilder = ImmutableList.builder();
        for (Map.Entry<String, String> entry : singleValueParams.entrySet())
        {
            listBuilder.add(Pair.of(Arrays.asList(entry.getKey().split("\\.")), entry.getValue()));
        }
        final Iterable<Pair<List<String>, String>> pairs = listBuilder.build();
        return  transformToNestedMap(pairs);
//        final ImmutableMultimap.Builder<String, Pair<List<String>, String>> builder = ImmutableMultimap.<String, Pair<List<String>, String>>builder();
//        for (Pair<List<String>, String> pair : pairs)
//        {
//            final List<String> pathComponents = pair.getLeft();
//            final String key = pathComponents.get(0);
//            final List<String> remainingPathComponents = pathComponents.subList(1, pathComponents.size());
//            final String value = pair.getRight();
//            builder.put(key, Pair.of(remainingPathComponents, value));
//        }
//
//        return Maps.transformValues(builder.build().asMap(), new Function<Collection<Pair<List<String>, String>>, Object>()
//        {
//            @Override
//            public Object apply(@Nullable Collection<Pair<List<String>, String>> input)
//            {
//                return transform(input);
//            }
//        });
    }

    private Map<String, Object> transformToNestedMap(Iterable<Pair<List<String>, String>> pairs)
    {
        // TODO: do some checking
        return (Map<String, Object>)transform(pairs);
    }

    private Object transform(Iterable<Pair<List<String>, String>> pairs) //throws MalformedRequestException
    {
//        final ImmutableMultimap.Builder<String, Pair<List<String>, String>> builder = ImmutableMultimap.<String, Pair<List<String>, String>>builder();
        /*
         cases:
           1/ empty list???
           2/ single entry
             a/ remainingpaths empty. Return value
             b/ remainingpaths !empty. return map
           3/ > 1 entry
             a/ remainingpaths empty. error
             b/ remainingpaths !empty. return map
         */
        if (Iterables.size(pairs) == 1)
        {
            final Pair<List<String>, String> pair = Iterables.getFirst(pairs, null);
            final List<String> pathComponents = pair.getLeft();
            final String value = pair.getRight();
            if (pathComponents.isEmpty())
            {
                return value;
            }
        }

        final ImmutableMultimap.Builder<String, Pair<List<String>, String>> builder = ImmutableMultimap.<String, Pair<List<String>, String>>builder();
        for (Pair<List<String>, String> pair : pairs)
        {
            final List<String> pathComponents = pair.getLeft();
            final String value = pair.getRight();
            if (pathComponents.isEmpty())
            {
                continue; // TODO: Should we throw error?
//                throw new MalformedRequestException("TODO");
            }
            final String key = pathComponents.get(0);
            final List<String> remainingPathComponents = pathComponents.subList(1, pathComponents.size());

            builder.put(key, Pair.of(remainingPathComponents, value));
        }

        return Maps.transformValues(builder.build().asMap(), new Function<Collection<Pair<List<String>, String>>, Object>()
        {
            @Override
            public Object apply(@Nullable Collection<Pair<List<String>, String>> input)
            {
                return transform(input);
            }
        });

    }

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

    public Map<String, Object> getParamsInPathFormAsObjectValues()
    {
        return requestParams;
    }

    // TODO: Remove??
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


    private Map<String, String[]> getContextAsStringArr()
    {
        final ImmutableMap.Builder<String, String[]> builder = ImmutableMap.<String, String[]>builder();
        for (Map.Entry<String, Object> entry : requestParams.entrySet())
        {
            final Object value = entry.getValue();
            final String key = entry.getKey();
            addToMap(key, value, builder);
        }
        return builder.build();
    }

    private void addToMap(String key, Object value, ImmutableMap.Builder<String, String[]> builder)
    {
        if (value instanceof String[])
        {
            builder.put(key, (String[]) value);
        }
        else if (value instanceof Map)
        {
            addFlattenedMap(key, (Map) value, builder);
        }
        else
        {
            builder.put(key, new String[] { ObjectUtils.toString(value) });
        }
    }

    private void addFlattenedMap(String key, Map<?, ?> map, ImmutableMap.Builder<String, String[]> builder)
    {
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            String newKey = key + '.' + entry.getKey().toString();
            addToMap(newKey, entry.getValue(), builder);
        }

    }

}
