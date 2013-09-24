package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UrlTemplateInstanceImpl implements UrlTemplateInstance
{
    public static final TypeReference<HashMap<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<HashMap<String, Object>>()
            {
            };

    private final String urlTemplate;
    private final Map<String, Object> context;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;

    public UrlTemplateInstanceImpl(UrlVariableSubstitutor urlVariableSubstitutor, ContextMapURLSerializer contextMapURLSerializer,
                                   String urlTemplate, Map<String, Object> context, String username) throws InvalidContextParameterException
    {
        this.urlTemplate = urlTemplate;
        this.context = contextMapURLSerializer.getAuthenticatedAddonParameters(extractContext(context), username);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.contextMapURLSerializer = contextMapURLSerializer;
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
        if (!requestParams.containsKey("context"))
        {
            return requestParams;
        }

        final String[] contextParam = (String[]) requestParams.get("context");
        if (ArrayUtils.isEmpty(contextParam))
            throw new InvalidContextParameterException("Empty context received");

        final String contextJsonStr = contextParam[0];
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            Map<String, Object> contextMap = objectMapper.readValue(contextJsonStr, MAP_TYPE_REFERENCE);
            final HashMap<String, Object> mutableParams = Maps.newHashMap(requestParams);
            mutableParams.remove("context");
            return ImmutableMap.<String, Object>builder().putAll(mutableParams).putAll(contextMap).build();
        }
        catch (IOException e)
        {
            throw new InvalidContextParameterException("Failed to parse context Json", e);
        }

    }

}
