package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 *
 */
public class JiraGeneralPage implements GeneralPage

{
    @Inject
    private AtlassianWebDriver driver;

    @Inject
    private PageBinder pageBinder;
    private final String pageKey;
    private final String linkText;

    @FindBy(id = "general_dropdown_linkId_drop")
    private WebElement remoteappsGeneralMenuLink;


    public JiraGeneralPage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    @Override
    public boolean isRemoteAppLinkPresent()
    {
        remoteappsGeneralMenuLink.click();
        try
        {
            return driver.elementExists(By.linkText(linkText));
        }
        finally
        {
            remoteappsGeneralMenuLink.click();
        }
    }

    @Override
    public RemoteAppTestPage clickRemoteAppLink()
    {
        remoteappsGeneralMenuLink.click();
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(RemoteAppTestPage.class, pageKey);
    }
}
