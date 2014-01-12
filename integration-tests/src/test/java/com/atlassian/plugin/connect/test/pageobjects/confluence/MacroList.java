package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

public class MacroList
{
    @ElementBy(className = "autocomplete-macros")
    private PageElement macroList;

    public boolean hasEntryWithKey(String macroKey)
    {
        PageElement macroEntry = macroList.find(By.className("autocomplete-macro-" + macroKey));
        return macroEntry.isVisible();
    }
}
