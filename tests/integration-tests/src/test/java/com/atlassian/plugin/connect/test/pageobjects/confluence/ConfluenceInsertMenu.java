package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.editor.toolbars.InsertDropdownMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class ConfluenceInsertMenu extends InsertDropdownMenu
{
    public boolean hasEntryWithKey(String macroKey)
    {
        try
        {
            getPageElement().find(By.className("macro-" + macroKey));
            return true;
        }
        catch (NoSuchElementException e)
        {
            return false;
        }
    }
}
