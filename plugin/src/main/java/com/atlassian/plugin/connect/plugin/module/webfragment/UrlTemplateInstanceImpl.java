package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.Builder;

public class UrlTemplateInstanceImpl implements UrlTemplateInstance
{
    private static final TypeReference<HashMap<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<HashMap<String, Object>>()
            {
            };

    private final String urlTemplate;
    private final Map<String, Object> context;
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    public UrlTemplateInstanceImpl(UrlVariableSubstitutor urlVariableSubstitutor, ContextMapURLSerializer contextMapURLSerializer,
                                   String urlTemplate, Map<String, Object> context, String username)
            throws InvalidContextParameterException, UnauthorisedException, ResourceNotFoundException
    {
        this.urlTemplate = urlTemplate;
        this.context = contextMapURLSerializer.getAuthenticatedAddonParameters(extractContext(context), username);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    @Override
    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    @Override
    public String getUrlString()
    {
        // TODO: Look at changing urlVariableSubstitutor to take Map<String, String[]>
        return urlVariableSubstitutor.replace(urlTemplate, context);
    }

    @Override
    public Set<String> getTemplateVariables()
    {
        return urlVariableSubstitutor.getContextVariables(urlTemplate);
    }

    private Map<String, Object> extractContext(Map<String, Object> requestParams) throws InvalidContextParameterException
    {
        if (!requestParams.containsKey(CONTEXT_PARAMETER_KEY))
        {
            return requestParams;
        }

        final String[] contextParam = (String[]) requestParams.get(CONTEXT_PARAMETER_KEY);
        if (ArrayUtils.isEmpty(contextParam))
            throw new InvalidContextParameterException("Empty context received");

        final String contextJsonStr = contextParam[0];
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Map<String, Object> contextMap = objectMapper.readValue(contextJsonStr, MAP_TYPE_REFERENCE);
            final Map<String, Object> mutableParams = Maps.newHashMap(requestParams);
            mutableParams.remove(CONTEXT_PARAMETER_KEY);
            return ImmutableMap.<String, Object>builder().putAll(mutableParams).putAll(contextMap).build();
        }
        catch (IOException e)
        {
            throw new InvalidContextParameterException("Failed to parse context Json", e);
        }

    }


    @Override
    public Map<String, String[]> getNonTemplateContextParameters()
    {
        Set<String> templateVariables = getTemplateVariables();
        Builder<String, String[]> builder = ImmutableMap.builder();
        final Set<Map.Entry<String, String[]>> requestParameters = getContextAsStringArr().entrySet();
        for (Map.Entry<String, String[]> entry : requestParameters)
        {
            // copy only these context parameters which aren't already a part of URL.
            if (!templateVariables.contains(entry.getKey()))
            {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private Map<String, String[]> getContextAsStringArr()
    {
        final Builder<String, String[]> builder = ImmutableMap.<String, String[]>builder();
        for (Map.Entry<String, Object> entry : context.entrySet())
        {
            final Object value = entry.getValue();
            final String key = entry.getKey();
            addToMap(key, value, builder);
        }
        return builder.build();
    }

    private void addToMap(String key, Object value, Builder<String, String[]> builder)
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

    private void addFlattenedMap(String key, Map<?, ?> map, Builder<String, String[]> builder)
    {
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            addToMap(key, entry.getKey(), entry.getValue(), builder);
        }

    }

    private void addToMap(String parentKey, Object subKey, Object value, Builder<String,String[]> builder)
    {
        String key = parentKey + '.' + subKey.toString();
        addToMap(key, value, builder);
    }


}
