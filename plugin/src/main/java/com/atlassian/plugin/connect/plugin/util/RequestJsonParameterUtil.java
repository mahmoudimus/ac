package com.atlassian.plugin.connect.plugin.util;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.iframe.context.InvalidContextParameterException;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class RequestJsonParameterUtil
{
    public static final String CONTEXT_PARAMETER_KEY = "product-context";
    private static final Joiner PATH_COMPONENT_JOINER = Joiner.on('.');
    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<HashMap<String, Object>>()
            {
            };

    private static final Logger log = LoggerFactory.getLogger(RequestJsonParameterUtil.class);

    // the product context may be passed from the client as a json object. If so we pull them out and pretend they were request params. Not pretty but hey
    public Map<String, String[]> tryExtractContextFromJson(Map<String, String[]> requestParams) throws InvalidContextParameterException
    {
        if (!requestParams.containsKey(CONTEXT_PARAMETER_KEY))
        {
            return ImmutableMap.copyOf(requestParams);
        }

        final String[] contextParam = requestParams.get(CONTEXT_PARAMETER_KEY);
        if (contextParam.length > 1)
        {
            throw new InvalidContextParameterException("Multiple product-context parameters not supported");
        }

        final String contextJsonStr = contextParam[0];
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Map<String, Object> contextMap = objectMapper.readValue(contextJsonStr, MAP_TYPE_REFERENCE);

            final Map<String, String[]> mutableParams = Maps.newHashMap(requestParams);
            mutableParams.remove(CONTEXT_PARAMETER_KEY);
            final Map<String, String[]> contextParams = transformToPathForm(contextMap);
            checkSameParams(mutableParams, contextParams);
            final Map<String, String[]> m = Maps.newHashMap();
            m.putAll(mutableParams);
            // context params take precedence and will overwrite any params with same key from the url query
            m.putAll(contextParams);
            return ImmutableMap.copyOf(m);
        }
        catch (IOException e)
        {
            throw new InvalidContextParameterException("Failed to parse context Json", e);
        }

    }

    private void checkSameParams(Map<String, String[]> mutableParams, Map<String, String[]> contextParams)
    {
        for (String key : contextParams.keySet())
        {
            if (mutableParams.containsKey(key))
            {
                log.warn("same parameter key ({}) found in both url query and context json. Value from URL query will be overridden", key);
            }
        }
    }

    private Map<String, String[]> transformToPathForm(Map<String, Object> nestedMapsParams)
    {
        final ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
        final Iterable<Pair<List<String>, String[]>> pairs = transformToPathFormPairs(nestedMapsParams);
        for (Pair<List<String>, String[]> pair : pairs)
        {
            builder.put(createPath(pair.getLeft()), pair.getRight());
        }

        return builder.build();
    }

    private String createPath(List<String> pathComponents)
    {
        return PATH_COMPONENT_JOINER.join(pathComponents);
    }

    private Iterable<Pair<List<String>, String[]>> transformToPathFormPairs(Map<?, ?> nestedMapsParams)
    {
        final ImmutableList.Builder<Pair<List<String>, String[]>> resultBuilder = ImmutableList.builder();

        for (final Map.Entry<?, ?> entry : nestedMapsParams.entrySet())
        {
            Iterable<Pair<List<String>, String[]>> pairs = transformNestedValue(entry.getValue());
            // prepend our key to list of path components
            final Iterable<Pair<List<String>, String[]>> result =
                    Iterables.transform(pairs, new Function<Pair<List<String>, String[]>, Pair<List<String>, String[]>>()
                    {
                        @Override
                        public Pair<List<String>, String[]> apply(@Nullable Pair<List<String>, String[]> pair)
                        {
                            checkNotNull(pair);
                            final ImmutableList.Builder<String> builder = ImmutableList.builder();
                            builder.add(ObjectUtils.toString(entry.getKey()));
                            builder.addAll(pair.getLeft());
                            return Pair.of((List<String>) builder.build(), pair.getValue());
                        }
                    });

            resultBuilder.addAll(result);
        }


        return resultBuilder.build();
    }

    private Iterable<Pair<List<String>, String[]>> transformNestedValue(Object value)
    {
        if (value instanceof Map)
        {
            return transformToPathFormPairs((Map<?, ?>) value);
        }

        String[] arrValue = toStringArray(value);

        return ImmutableList.of(Pair.of((List<String>) ImmutableList.<String>of(), arrValue));
    }

    private String[] toStringArray(Object value)
    {
        if (value instanceof String[])
        {
            return (String[]) value;
        }

        if (value.getClass().isArray())
        {
            return iterableToStringArray(Arrays.asList((Object[])value));
        }

        if (value instanceof Iterable<?>)
        {
            return iterableToStringArray((Iterable<?>)value);
        }


        return new String[]{value.toString()};
    }

    private String[] iterableToStringArray(Iterable<?> iterable)
    {
        final Iterable<String> strings = Iterables.transform(iterable, new Function<Object, String>()
        {
            @Override
            public String apply(@Nullable Object input)
            {
                return input == null ? "" : input.toString();
            }
        });
        return Iterables.toArray(strings, String.class);
    }

}
