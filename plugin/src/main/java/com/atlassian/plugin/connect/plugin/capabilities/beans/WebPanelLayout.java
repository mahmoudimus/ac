package com.atlassian.plugin.connect.plugin.capabilities.beans;

public class WebPanelLayout
{
    private final String width;
    private final String minWidth;
    private final String height;
    private final String minHeight;

    public WebPanelLayout()
    {
        this.width = "";
        this.minWidth = "";
        this.height = "";
        this.minHeight = "";
    }

    public WebPanelLayout(String width, String minWidth, String height, String minHeight)
    {
        this.width = width;
        this.minWidth = minWidth;
        this.height = height;
        this.minHeight = minHeight;
    }

    public String getWidth()
    {
        return width;
    }

    public String getMinWidth()
    {
        return minWidth;
    }

    public String getHeight()
    {
        return height;
    }

    public String getMinHeight()
    {
        return minHeight;
    }
}
