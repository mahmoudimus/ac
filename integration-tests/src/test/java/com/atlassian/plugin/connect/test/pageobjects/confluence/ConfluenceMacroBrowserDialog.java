package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import java.util.concurrent.TimeUnit;

public class ConfluenceMacroBrowserDialog extends MacroBrowserDialog
{
    @ElementBy(className = "ok")
    private PageElement saveButton;

    public void clickSave()
    {
        saveButton.timed().isVisible().by(20, TimeUnit.SECONDS);
        clickButton("ok", true);
    }
}
