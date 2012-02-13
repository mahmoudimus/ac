package com.atlassian.labs.remoteapps.modules.confluence;

import java.net.URI;

/**
 * Base for remote macros
 */
public abstract class AbstractRemoteMacro implements RemoteMacro
{

    protected final RemoteMacroInfo remoteMacroInfo;

    public AbstractRemoteMacro(RemoteMacroInfo remoteMacroInfo)
    {
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

    @Override
    public URI getBaseUrl()
    {
        return remoteMacroInfo.getApplicationLinkOperations().get().getRpcUrl();
    }
}
