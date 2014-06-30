package com.atlassian.plugin.connect.test.pageobjects;

public class ConnectAddOnTestPage extends ConnectAddOnEmbeddedTestPage
{
    public ConnectAddOnTestPage(String pageKey, boolean includeEmbeddedPrefix)
    {
        super(pageKey, includeEmbeddedPrefix);
    }

    public ConnectAddOnTestPage(String pageKey, String addOnKey, boolean includeEmbeddedPrefix)
    {
        super(pageKey, addOnKey, includeEmbeddedPrefix);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
