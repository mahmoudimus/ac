package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.macro.Macro;

public abstract class AbstractMacro implements Macro
{
    private final BodyType bodyType;
    private final OutputType outputType;

    protected AbstractMacro(BodyType bodyType, OutputType outputType)
    {
        this.bodyType = bodyType;
        this.outputType = outputType;
    }

    @Override
    public BodyType getBodyType()
    {
        return bodyType;
    }

    @Override
    public OutputType getOutputType()
    {
        return outputType;
    }

}
