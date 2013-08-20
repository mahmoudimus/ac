package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.webdriver.AtlassianWebDriver;

public class RemotePluginTestPage extends RemotePluginEmbeddedTestPage
{
    @Inject
    AtlassianWebDriver driver;

    public RemotePluginTestPage(String pageKey)
    {
        super("servlet-" + pageKey);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
