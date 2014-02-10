package com.atlassian.plugin.connect.plugin.iframe.context;

public class ModuleViewParameters
{
    private String width;
    private String height;

    public ModuleViewParameters()
    {
    }

    public ModuleViewParameters(String width, String height)
    {
        this.width = width;
        this.height = height;
    }

    public String getWidth()
    {
        return width;
    }

    public void setWidth(String width)
    {
        this.width = width;
    }

    public String getHeight()
    {
        return height;
    }

    public void setHeight(String height)
    {
        this.height = height;
    }
}
