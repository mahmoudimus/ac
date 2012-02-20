package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.annotation.Nullable;
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
    
    @Init
    public void init()
    {
        // we do this because sometimes we try to click the dropdown when it isn't ready, and the
        // default href is '#'
        driver.executeScript("document.getElementById('general_dropdown_linkId_drop').setAttribute('href', 'javascript:void(0)');");
    }

    @Override
    public boolean isRemoteAppLinkPresent()
    {
        openMenu();
        return driver.elementExists(By.linkText(linkText));
    }

    @Override
    public RemoteAppTestPage clickRemoteAppLink()
    {
        openMenu();
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(RemoteAppTestPage.class, pageKey);
    }

    private void openMenu()
    {
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(@Nullable WebDriver from)
            {
                remoteappsGeneralMenuLink.click();
                return driver.elementIsVisible(By.linkText(linkText));
            }
        });
    }
}
