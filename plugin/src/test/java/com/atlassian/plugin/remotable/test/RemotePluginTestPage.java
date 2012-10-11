package com.atlassian.plugin.remotable.test;

import com.atlassian.webdriver.AtlassianWebDriver;

import javax.inject.Inject;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 21/02/12 Time: 12:47 AM To change this template use
 * File | Settings | File Templates.
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
