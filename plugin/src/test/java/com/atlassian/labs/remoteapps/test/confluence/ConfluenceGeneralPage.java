package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.labs.remoteapps.test.GeneralPage;
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

    @FindBy(id = "browse-menu-link")
    private WebElement browseMenuLink;


    public ConfluenceGeneralPage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    @Override
    public boolean isRemoteAppLinkPresent()
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
    public RemoteAppTestPage clickRemoteAppLink()
    {
        browseMenuLink.click();
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(RemoteAppTestPage.class, pageKey);
    }
}
