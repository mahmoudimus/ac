package com.atlassian.plugin.remotable.test;

import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

/**
 */
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
