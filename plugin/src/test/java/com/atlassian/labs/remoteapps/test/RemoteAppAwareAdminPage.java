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
public class RemoteAppAwareAdminPage
{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;

    @FindBy(linkText = "Remote App app1 Admin")
    WebElement adminLink;

    public boolean isRemoteAppLinkPresent()
    {
        return driver.elementExists(By.linkText("Remote App app1 Admin"));
    }

    public MyIframePage clickRemoteAppAdminLink()
    {
        adminLink.click();
        return pageBinder.bind(MyIframePage.class);
    }
}
