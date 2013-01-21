package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.pageobjects.binder.Init;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

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
