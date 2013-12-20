package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

/**
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#PANEL_LAYOUT_EXAMPLE}
 * @schemaTitle Web Panel Layout
 */
public class WebPanelLayout
{
    private final String width;
    private final String height;

    public WebPanelLayout()
    {
        this.width = "";
        this.height = "";
    }

    public WebPanelLayout(String width, String height)
    {
        this.width = width;
        this.height = height;
    }

    public String getWidth()
    {
        return width;
    }

    public String getHeight()
    {
        return height;
    }
}
