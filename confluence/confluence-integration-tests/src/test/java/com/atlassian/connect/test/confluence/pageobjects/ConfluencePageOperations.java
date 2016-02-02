package com.atlassian.connect.test.confluence.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import com.atlassian.webdriver.AtlassianWebDriver;

import com.google.inject.Inject;

import org.openqa.selenium.By;

public class ConfluencePageOperations extends ConnectPageOperations
{
    @Inject
    public ConfluencePageOperations(PageBinder pageBinder, AtlassianWebDriver driver)
    {
        super(pageBinder, driver);
    }

    public RemotePluginDialog editMacro(String macroKey)
    {
        String macroNodeSelector = "$(\"#wysiwygTextarea_ifr\").contents().find(\"table[data-macro-name='" + macroKey + "']\")";
        driver.executeScript("tinymce.confluence.macrobrowser.editMacro(" + macroNodeSelector + ")");
        return findDialog(macroKey);
    }

    public void reorderConfluenceTableOnPage()
    {
        driver.findElement(By.className("tablesorter-header-inner")).click();
    }
}
