package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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

    public JiraGeneralPage(String pageKey, String linkText)
    {
        this.pageKey = pageKey;
        this.linkText = linkText;
    }

    @Override
    public boolean isRemotePluginLinkPresent()
    {
        return driver.elementExists(By.linkText(linkText)) || (driver.elementExists(By.linkText("More")) && driver.elementExists(By.linkText(linkText)));
    }

    @Override
    public RemotePluginTestPage clickRemotePluginLink()
    {
        if (driver.elementExists(By.linkText(linkText)))
        {
            driver.findElement(By.linkText(linkText)).click();
        }
        else
        {
            driver.findElement(By.linkText("More")).click();
            driver.findElement(By.linkText(linkText)).click();
        }
        return pageBinder.bind(RemotePluginTestPage.class, pageKey);
    }
}
