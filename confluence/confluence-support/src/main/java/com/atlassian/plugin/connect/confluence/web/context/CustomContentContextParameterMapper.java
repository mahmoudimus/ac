package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.content.CustomContentEntityObject;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@ConfluenceComponent
public class CustomContentContextParameterMapper implements TypeBasedConnectContextParameterMapper<CustomContentEntityObject>
{

    private final Set<Parameter<CustomContentEntityObject>> parameters;
    private final ContentContextParameterMapper contentContextParameterMapper;

    @Autowired
    public CustomContentContextParameterMapper(ContentContextParameterMapper contentContextParameterMapper,
            CustomContentParameter... parameters)
    {
        this.contentContextParameterMapper = contentContextParameterMapper;
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return contentContextParameterMapper.getContextKey();
    }

    @Override
    public Class<CustomContentEntityObject> getContextValueClass()
    {
        return CustomContentEntityObject.class;
    }

    @Override
    public Set<Parameter<CustomContentEntityObject>> getParameters()
    {
        return parameters;
    }

    public static interface CustomContentParameter extends ConnectContextParameterMapper.Parameter<CustomContentEntityObject>
    {}
}
