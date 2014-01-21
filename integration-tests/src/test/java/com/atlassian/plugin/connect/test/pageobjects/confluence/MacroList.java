package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class MacroList
{
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    PageElementFinder pageElementFinder;

    public boolean hasEntryWithKey(String macroKey)
    {
        driver.switchTo().defaultContent();
        PageElement macroList = pageElementFinder.find(By.className("autocomplete-macros"));
        PageElement macroEntry = macroList.find(By.className("autocomplete-macro-" + macroKey));
        return macroEntry.timed().isVisible().byDefaultTimeout();
    }
}
