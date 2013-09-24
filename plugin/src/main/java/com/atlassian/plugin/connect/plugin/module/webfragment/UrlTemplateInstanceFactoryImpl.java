package com.atlassian.plugin.connect.plugin.module.webfragment;

import com.atlassian.plugin.connect.plugin.module.context.ContextMapURLSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UrlTemplateInstanceFactoryImpl implements UrlTemplateInstanceFactory
{
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ContextMapURLSerializer contextMapURLSerializer;

    @Autowired
    public UrlTemplateInstanceFactoryImpl(UrlVariableSubstitutor urlVariableSubstitutor, ContextMapURLSerializer contextMapURLSerializer)
    {
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.contextMapURLSerializer = contextMapURLSerializer;
    }

    @Override
    public UrlTemplateInstance create(String urlTemplateString, Map<String, Object> requestParams, String username) throws InvalidContextParameterException
    {
        return new UrlTemplateInstanceImpl(urlVariableSubstitutor, contextMapURLSerializer, urlTemplateString, requestParams, username);
    }

}
