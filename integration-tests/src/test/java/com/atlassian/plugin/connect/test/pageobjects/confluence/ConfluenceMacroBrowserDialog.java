package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;

public class ConfluenceMacroBrowserDialog extends MacroBrowserDialog
{
    public void clickInsert()
    {
        clickButton("ok", true);
    }
}
