package com.atlassian.labs.remoteapps.plugin.module.confluence;

import com.atlassian.labs.remoteapps.plugin.RemoteAppAccessorFactory;

import java.net.URI;

/**
 * Base for remote macros
 */
public abstract class AbstractRemoteMacro implements RemoteMacro
{
    protected final RemoteAppAccessorFactory remoteAppAccessorFactory;
    protected final RemoteMacroInfo remoteMacroInfo;

    public AbstractRemoteMacro(RemoteAppAccessorFactory remoteAppAccessorFactory,
            RemoteMacroInfo remoteMacroInfo)
    {
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;
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
        return remoteAppAccessorFactory.get(remoteMacroInfo.getPluginKey()).getDisplayUrl();
    }
}
