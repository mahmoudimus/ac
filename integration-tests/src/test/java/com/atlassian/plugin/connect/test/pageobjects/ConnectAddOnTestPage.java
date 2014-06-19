package com.atlassian.plugin.connect.test.pageobjects;

public class ConnectAddOnTestPage extends ConnectAddOnEmbeddedTestPage
{
    public ConnectAddOnTestPage(String pageKey)
    {
        super(pageKey);
    }

    public ConnectAddOnTestPage(String pageKey, String extraPrefix)
    {
        super(pageKey, extraPrefix);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
