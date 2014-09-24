package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.editor.InsertMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class ConfluenceInsertMenu extends InsertMenu
{
    public boolean hasEntryWithKey(String macroKey)
    {
        try
        {
            dropdownMenu.find(By.className("macro-" + macroKey));
            return true;
        }
        catch (NoSuchElementException e)
        {
            return false;
        }
    }
}
