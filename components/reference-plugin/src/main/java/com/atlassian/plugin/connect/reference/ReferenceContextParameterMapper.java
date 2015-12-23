package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterMapper;
import com.atlassian.plugin.connect.spi.web.context.TypeBasedConnectContextParameterMapper;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ReferenceContextParameterMapper implements TypeBasedConnectContextParameterMapper<Plugin>
{

    private static final String CONTEXT_KEY = "plugin";

    private final Set<Parameter<Plugin>> parameters;

    @Autowired
    public ReferenceContextParameterMapper(PluginParameter... parameters)
    {
        this.parameters = Sets.newHashSet(parameters);
    }

    @Override
    public String getContextKey()
    {
        return CONTEXT_KEY;
    }

    @Override
    public Class<Plugin> getContextValueClass()
    {
        return Plugin.class;
    }

    @Override
    public Set<Parameter<Plugin>> getParameters()
    {
        return parameters;
    }

    public static interface PluginParameter extends ConnectContextParameterMapper.Parameter<Plugin>
    {}
}
