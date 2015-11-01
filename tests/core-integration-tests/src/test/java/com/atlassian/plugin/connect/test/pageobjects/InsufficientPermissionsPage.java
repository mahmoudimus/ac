package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.plugin.connect.test.utils.IframeUtils;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class InsufficientPermissionsPage implements Page
{
    @Inject
    protected AtlassianWebDriver driver;

    private final String appKey;
    private final String pageKey;

    public InsufficientPermissionsPage(String appKey, String pageKey)
    {
        this.appKey = appKey;
        this.pageKey = pageKey;
    }

    @Override
    public String getUrl()
    {
        return IframeUtils.iframeServletPath(appKey, pageKey);
    }

    @WaitUntil
    public void waitForInit()
    {
        driver.waitUntilElementIsLocated(By.id("errorMessage"));
    }

    public String getErrorMessage()
    {
        return driver.findElement(By.id("errorMessage")).getText();
    }

}
