package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.macro.Macro;

/**
 * Base for remote macros
 */
public abstract class AbstractRemoteMacro implements Macro
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
}
