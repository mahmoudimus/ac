package com.atlassian.plugin.remotable.test.pageobjects.refapp;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.remotable.test.pageobjects.GeneralPage;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 *
 */
public class RefappGeneralPage implements GeneralPage

{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;

    public RefappGeneralPage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return driver.elementExists(By.linkText(linkText));
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
    }
}
