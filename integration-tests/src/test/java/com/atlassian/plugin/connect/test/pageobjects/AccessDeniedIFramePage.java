package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;

public class AccessDeniedIFramePage implements Page
{
    private final String pageKey;
    private final String appKey;
    @Inject
    private AtlassianWebDriver driver;

    public AccessDeniedIFramePage(String appKey, String pageKey)
    {
        this.pageKey = pageKey;
        this.appKey = appKey;
    }


    @Override
    public String getUrl()
    {
        return "/plugins/servlet/atlassian-connect/" + appKey + "/" + pageKey;
    }

    public boolean isIframeAvailable()
    {
        return driver.elementExists(By.id("embedded-" + pageKey));
    }
}
