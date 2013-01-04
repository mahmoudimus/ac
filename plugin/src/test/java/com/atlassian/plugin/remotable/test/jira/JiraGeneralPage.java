package com.atlassian.plugin.remotable.test.jira;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.webdriver.AtlassianWebDriver;

import com.google.common.base.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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

    private WebElement remotepluginsGeneralMenuLink;

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
        driver.executeScript("(function () { var target = document.getElementById('general_dropdown_linkId_drop'); if (target) target.setAttribute('href', 'javascript:void(0)'); }());");
        remotepluginsGeneralMenuLink = driver.elementExists(By.id("general_dropdown_linkId_drop")) ?
                                       driver.findElement(By.id("general_dropdown_linkId_drop")) :
                                       driver.findElement(By.id("general_dropdown_linkId"));
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        openMenu();
        return driver.elementExists(By.linkText(linkText));
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        openMenu();
        driver.waitUntilElementIsLocated(By.linkText(linkText));
        driver.findElement(By.linkText(linkText)).click();
        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
    }

    private void openMenu()
    {
        driver.waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(@Nullable WebDriver from)
            {
                remotepluginsGeneralMenuLink.click();
                return driver.elementIsVisible(By.linkText(linkText));
            }
        });
    }
}
