package com.atlassian.plugin.connect.test.pageobjects.confluence;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.plugin.connect.test.pageobjects.GeneralPage;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 */
public class ConfluenceGeneralPage implements GeneralPage

{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;

    private WebElement browseMenuLink;


    public ConfluenceGeneralPage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    @Init
    public void init()
    {
        By browseLocator = By.id("browse-menu-link");
        if (driver.elementExists(browseLocator))
        {
            browseMenuLink = driver.findElement(browseLocator);
        }
        else
        {
            browseMenuLink = driver.findElement(By.id("help-menu-link"));
        }
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        browseMenuLink.click();
        try
        {
            return driver.elementExists(By.linkText(linkText));
        }
        finally
        {
            browseMenuLink.click();
        }
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        browseMenuLink.click();
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
    }
}
