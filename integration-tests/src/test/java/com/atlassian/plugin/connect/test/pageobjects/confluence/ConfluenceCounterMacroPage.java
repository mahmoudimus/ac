package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 *
 */
public class ConfluenceCounterMacroPage implements Page
{
    @Inject
    AtlassianWebDriver driver;

    @Inject
    PageBinder pageBinder;
    private String title;

    public ConfluenceCounterMacroPage(String title)
    {
        this.title = title;
    }

    @Override
    public String getUrl()
    {
        return "/display/ds/" + title;
    }

    public String getCounterMacroBody()
    {
        driver.waitUntilElementIsNotLocated(By.className("bp-loading"));
        return driver.findElement(By.className("rp-counter")).getText();
    }

}
