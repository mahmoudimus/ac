package com.atlassian.plugin.remotable.test.confluence;

import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 *
 */
public class ConfluenceMacroTestSuitePage extends ConfluenceMacroPage
{
    public ConfluenceMacroTestSuitePage(String title)
    {
        super(title);
    }

    public String getPageIdFromMacro()
    {
        return driver.findElement(By.className("rp-page-id")).getText();
    }

    public String getPageIdFromMacroInComment()
    {
        return driver.findElement(By.className("rp-comment")).findElement(By.className("rp-page-id")).getText();
    }

    public String getBodyNoteFromMacro()
    {

        WebElement body = driver.findElement(By.className("rp-body"));
        return (!body.findElements(By.className("panelMacro")).isEmpty() ?
                body.findElement(By.className("panelMacro")) :
                body.findElement(By.className("message-content"))).getText();
    }

    public String getSlowMacroBody()
    {
        return driver.findElement(By.className("slow-macro")).getText();
    }

    public String getImageMacroAlt()
    {
        return driver.findElement(By.className("image-macro")).findElement(By.tagName("img")).getAttribute("alt");
    }

    public RemotePluginTestPage visitGeneralLink()
    {
//        By browseLocator = By.id("browse-menu-link");
        WebElement menuLink;
//        if (driver.elementExists(browseLocator))
//        {
//            menuLink = driver.findElement(browseLocator);
//        }
//        else
//        {
            menuLink = driver.findElement(By.id("help-menu-link"));
//        }
        menuLink.click();
        driver.findElement(By.id("webitem-remotePluginGeneral")).click();
        return pageBinder.bind(RemotePluginTestPage.class, "remotePluginGeneral");
    }
}
