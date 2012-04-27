package com.atlassian.labs.remoteapps.test;

import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 21/02/12 Time: 12:47 AM To change this template use
 * File | Settings | File Templates.
 */
public class RemoteAppTestPage extends RemoteAppEmbeddedTestPage
{
    @Inject
    AtlassianWebDriver driver;

    public RemoteAppTestPage(String pageKey)
    {
        super("servlet-" + pageKey);
    }

    public String getTitle()
    {
        return driver.getTitle();
    }
}
