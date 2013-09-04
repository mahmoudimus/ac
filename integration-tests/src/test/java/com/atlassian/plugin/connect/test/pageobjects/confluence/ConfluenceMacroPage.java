package com.atlassian.plugin.connect.test.pageobjects.confluence;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.Check;

import org.openqa.selenium.By;

/**
 *
 */
public class ConfluenceMacroPage implements Page
{
    @Inject
    AtlassianWebDriver driver;

    @Inject
    PageBinder pageBinder;
    private String title;

    public ConfluenceMacroPage(String title)
    {
        this.title = title;
    }

    @WaitUntil
    public void waitForBigPipe()
    {
//        if(driver.elementExists(By.className("bp-loading")))
//        {
//            driver.waitUntilElementIsNotLocated(By.className("bp-loading"));
//        }

        driver.waitUntilElementIsNotLocated(By.className("bp-loading"));
    }

    @Override
    public String getUrl()
    {
        return "/display/ds/" + title;
    }

    public String getMacroError(String macroKey)
    {
        return driver.findElement(By.className(macroKey + "-macro")).getText();
    }

}
