package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class UrlTemplateInstance
{
    private final String urlTemplate;
    private final Map<String, Object> context;
    private final Map<String, String[]> contextAsStringArr;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;

    public UrlTemplateInstance(String urlTemplate, Map<String, Object> context,
                               UrlVariableSubstitutor urlVariableSubstitutor,
                               ContextMapURLSerializer contextMapURLSerializer, String username)
    {
        this.urlTemplate = urlTemplate;
        this.context = contextMapURLSerializer.getAuthenticatedAddonParameters(context, username);
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.contextMapURLSerializer = contextMapURLSerializer;
        // damn generics
        this.contextAsStringArr = Maps.transformValues(this.context, new Function<Object, String[]>()
        {
            @Override
            public String[] apply(@Nullable Object input)
            {
                return (String[]) input;
            }
        });
    }

    public String getUrlTemplate()
    {
        return urlTemplate;
    }

    public String getUrlString()
    {
        // TODO: Look at changing urlVariableSubstitutor to take Map<String, String[]>
        return urlVariableSubstitutor.replace(urlTemplate, context);
    }

    public Set<String> getTemplateVariables()
    {
        return urlVariableSubstitutor.getContextVariables(urlTemplate);
    }

    public Map<String, String[]> getNonTemplateContextParameters()
    {
        Set<String> templateVariables = getTemplateVariables();
        ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
        final Set<Map.Entry<String, String[]>> requestParameters = contextAsStringArr.entrySet();
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
