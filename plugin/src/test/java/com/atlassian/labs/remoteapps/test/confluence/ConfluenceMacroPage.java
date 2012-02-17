package com.atlassian.labs.remoteapps.test.confluence;

import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

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

    @Override
    public String getUrl()
    {
        return "/display/ds/" + title;
    }

    public String getPageIdFromMacro()
    {
        return driver.findElement(By.className("rp-page-id")).getText();
    }

    public String getBodyNoteFromMacro()
    {
        return driver.findElement(By.className("rp-body")).findElement(By.className("panelMacro")).getText();
    }

    public String getSlowMacroBody()
    {
        return driver.findElement(By.className("slow-macro")).getText();
    }

    public String getImageMacroAlt()
    {
        return driver.findElement(By.className("image-macro")).findElement(By.tagName("img")).getAttribute("alt");
    }

    public RemoteAppTestPage visitGeneralLink()
    {
        driver.findElement(By.id("browse-menu-link")).click();
        driver.findElement(By.id("webitem-remoteAppGeneral")).click();
        return pageBinder.bind(RemoteAppTestPage.class, "remoteAppGeneral");
    }
}
