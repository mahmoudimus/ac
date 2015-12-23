package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@ConfluenceComponent
public class ContentContextParameterMapper implements TypeBasedConnectContextParameterMapper<ContentEntityObject>
{

    private static final String CONTEXT_KEY = "content";

    private final Set<Parameter<ContentEntityObject>> parameters;

    @Autowired
    public ContentContextParameterMapper(ContentParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<ContentEntityObject> getContextValueClass()
    {
        return ContentEntityObject.class;
    }

    @Override
    public Set<Parameter<ContentEntityObject>> getParameters()
    {
        return parameters;
    }

    public static interface ContentParameter extends ConnectContextParameterMapper.Parameter<ContentEntityObject>
    {}
}
