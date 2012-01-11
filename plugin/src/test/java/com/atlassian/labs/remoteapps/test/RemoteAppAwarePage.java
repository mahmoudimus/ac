package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 *
 */
public class RemoteAppAwarePage
{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;


    public RemoteAppAwarePage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    public boolean isRemoteAppLinkPresent()
    {
        return driver.elementExists(By.linkText(linkText));
    }

    public MyIframePage clickRemoteAppLink()
    {
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(MyIframePage.class, "servlet-" + pageKey);
    }
}
