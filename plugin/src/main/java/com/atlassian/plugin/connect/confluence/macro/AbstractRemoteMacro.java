package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.connect.plugin.module.confluence.RemoteMacroInfo;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;

import java.net.URI;

/**
 * Base for remote macros
 */
public abstract class AbstractRemoteMacro implements Macro
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
    public BodyType getBodyType()
    {
        return remoteMacroInfo.getBodyType();
    }

    @Override
    public OutputType getOutputType()
    {
        return remoteMacroInfo.getOutputType();
    }

    public URI getBaseUrl()
    {
        return remotablePluginAccessorFactory.get(remoteMacroInfo.getPluginKey()).getBaseUrl();
    }
}
