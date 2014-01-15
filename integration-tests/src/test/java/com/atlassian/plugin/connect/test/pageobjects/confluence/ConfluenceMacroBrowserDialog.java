package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

public class ConfluenceMacroBrowserDialog extends MacroBrowserDialog
{
    public void selectAndInsertMacro(String macroKey)
    {
        selectMacro(macroKey);
        insertMacro();
    }

    public void selectMacro(String macroKey)
    {
        PageElement macro = pageElementFinder.find(By.id("macro-" + macroKey));
        macro.click();
    }

    public void insertMacro()
    {
        clickButton("ok", true);
    }
}
