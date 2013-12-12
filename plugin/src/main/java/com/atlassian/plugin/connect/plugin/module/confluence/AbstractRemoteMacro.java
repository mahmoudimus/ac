package com.atlassian.plugin.connect.plugin.module.confluence;

import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;

import java.net.URI;

/**
 * Base for remote macros
 */
public abstract class AbstractRemoteMacro implements RemoteMacro
{
    protected final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    protected final RemoteMacroInfo remoteMacroInfo;

    public AbstractRemoteMacro(RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            RemoteMacroInfo remoteMacroInfo)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.remoteMacroInfo = remoteMacroInfo;
    }

    @Override
    public RemoteMacroInfo getRemoteMacroInfo()
    {
        return remoteMacroInfo;
    }

    @Override
    public BodyType getBodyType()
    {
        return remoteMacroInfo.getBodyType();
    }

    @Override
    public OutputType getOutputType()
    {
        return remoteMacroInfo.getOutputType();
    }

    @Override
    public URI getBaseUrl()
    {
        return remotablePluginAccessorFactory.get(remoteMacroInfo.getPluginKey()).getBaseUrl();
    }
}
