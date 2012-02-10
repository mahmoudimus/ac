package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.Page;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

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
        return "/plugins/servlet/remoteapps/" + appKey + "/" + pageKey;
    }

    public boolean isIframeAvailable()
    {
        return driver.elementExists(By.id("embedded-" + pageKey));
    }
}
