package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

@ConfluenceComponent
public class SpaceContextParameterMapper implements TypeBasedConnectContextParameterMapper<Space>
{

    private static final String CONTEXT_KEY = "space";

    private final Set<Parameter<Space>> parameters;

    @Autowired
    public SpaceContextParameterMapper(SpaceParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<Space> getContextValueClass()
    {
        return Space.class;
    }

    @Override
    public Set<Parameter<Space>> getParameters()
    {
        return parameters;
    }

    public static interface SpaceParameter extends ConnectContextParameterMapper.Parameter<Space>
    {}
}
