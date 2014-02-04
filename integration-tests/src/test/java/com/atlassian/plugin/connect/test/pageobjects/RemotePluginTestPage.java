package com.atlassian.plugin.connect.test.pageobjects;

public class RemotePluginTestPage extends RemotePluginEmbeddedTestPage
{
    public RemotePluginTestPage(String pageKey)
    {
        super(pageKey);
    }

    public RemotePluginTestPage(String pageKey, String extraPrefix)
    {
        super(pageKey, extraPrefix);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
