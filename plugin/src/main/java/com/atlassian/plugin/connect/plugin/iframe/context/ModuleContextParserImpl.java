package com.atlassian.plugin.connect.plugin.iframe.context;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 *
 */
@Component
public class ModuleContextParserImpl implements ModuleContextParser
{
    public static final String CONTEXT_PARAMETER_KEY = "product-context";
    private static final Joiner PATH_COMPONENT_JOINER = Joiner.on('.');
    private static final Logger log = LoggerFactory.getLogger(ModuleContextParserImpl.class);
    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<HashMap<String, Object>>()
            {
            };


    private final ModuleContextFilter moduleContextFilter;

    @Autowired
    public ModuleContextParserImpl(ModuleContextFilter moduleContextFilter)
    {
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public ModuleContextParameters parseContextParameters(final HttpServletRequest req)
    {
        ModuleContextParameters unfiltered = new HashMapModuleContextParameters();
        final Map<String, String[]> parameterMap = tryExtractContextFromJson(req.getParameterMap());
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
        {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values.length > 1)
            {
                log.warn("Multiple parameters with the same name are not supported, only the first will be used. "
                        + "(key was " + key + ")");
            }
            unfiltered.put(key, values[0]);
        }
        return moduleContextFilter.filter(unfiltered);
    }


    // the product context may be passed from the client as a json object. If so we pull them out and pretend they were request params. Not pretty but hey
    private Map<String, String[]> tryExtractContextFromJson(Map<String, String[]> requestParams) throws InvalidContextParameterException
    {
        if (!requestParams.containsKey(CONTEXT_PARAMETER_KEY))
        {
            return requestParams;
        }

        final String[] contextParam = requestParams.get(CONTEXT_PARAMETER_KEY);
        if (contextParam == null)
            return ImmutableMap.copyOf(requestParams);

        final String contextJsonStr = contextParam[0];
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Map<String, String[]> contextMap = objectMapper.readValue(contextJsonStr, MAP_TYPE_REFERENCE);

            final Map<String, String[]> mutableParams = Maps.newHashMap(requestParams);
            mutableParams.remove(CONTEXT_PARAMETER_KEY);
            return ImmutableMap.<String, String[]>builder()
                    .putAll(mutableParams)
                    .putAll(transformToPathForm(contextMap))
                    .build();
        }
        catch (IOException e)
        {
            throw new InvalidContextParameterException("Failed to parse context Json", e);
        }

    }

    private Map<String, String[]> transformToPathForm(Map<String, String[]> nestedMapsParams)
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
            return transformToPathFormPairs((Map<?, ?>) value);

        String[] arrValue = value instanceof String[] ? (String[]) value : new String[]{value.toString()};

        return ImmutableList.of(Pair.of((List<String>) ImmutableList.<String>of(), arrValue));
    }

}
