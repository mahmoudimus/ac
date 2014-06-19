package com.atlassian.plugin.connect.test.pageobjects;

public class ConnectAddOnTestPage extends ConnectAddOnEmbeddedTestPage
{
    public ConnectAddOnTestPage(String pageKey)
    {
        super(pageKey, true);
    }

    public ConnectAddOnTestPage(String pageKey, String extraPrefix)
    {
        super(pageKey, extraPrefix, true);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
