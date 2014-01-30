package com.atlassian.plugin.connect.test.pageobjects;

public class RemotePluginTestPage extends RemotePluginEmbeddedTestPage
{
    public RemotePluginTestPage(String pageKey)
    {
        super(pageKey);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
