package com.atlassian.plugin.connect.plugin.iframe.context;

public class ModuleViewParameters
{
    private final String uiParams;

    public ModuleViewParameters(String uiParams)
    {
        this.uiParams = uiParams;
    }

    public String getUiParams()
    {
        return uiParams;
    }
}
