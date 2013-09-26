package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.atlassian.plugin.connect.plugin.module.context.MalformedRequestException;
import com.atlassian.plugin.connect.plugin.module.context.ResourceNotFoundException;
import com.atlassian.plugin.connect.plugin.module.permission.UnauthorisedException;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.Builder;

public class UrlTemplateInstanceImpl implements UrlTemplateInstance
{
    private final String urlTemplate;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final RequestParameterHelper requestParameterHelper;

    public UrlTemplateInstanceImpl(UrlVariableSubstitutor urlVariableSubstitutor, ContextMapURLSerializer contextMapURLSerializer,
                                   String urlTemplate, Map<String, Object> context, String username)
            throws MalformedRequestException, UnauthorisedException, ResourceNotFoundException
    {
        this.urlTemplate = urlTemplate;
        requestParameterHelper = new RequestParameterHelper(context);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        authenticateContextParams(contextMapURLSerializer, username);
    }

    private void authenticateContextParams(ContextMapURLSerializer contextMapURLSerializer, String username)
            throws ResourceNotFoundException, UnauthorisedException, MalformedRequestException
    {
        contextMapURLSerializer.getAuthenticatedAddonParameters(requestParameterHelper.getParamsInNestedForm(), username);
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
        return urlVariableSubstitutor.replace(urlTemplate, requestParameterHelper.getParamsInPathFormAsObjectValues());
    }

    @Override
    public Set<String> getTemplateVariables()
    {
        return urlVariableSubstitutor.getContextVariables(urlTemplate);
    }

    @Override
    public Map<String, String[]> getNonTemplateContextParameters()
    {
        Set<String> templateVariables = getTemplateVariables();
        Builder<String, String[]> builder = ImmutableMap.builder();
        final Set<Map.Entry<String, String[]>> requestParameters = requestParameterHelper.getParamsInPathForm().entrySet();
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



}
