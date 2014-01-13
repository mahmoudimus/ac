package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.pageobjects.PageBinder;

import javax.inject.Inject;

public class ConfluenceMacroBrowserDialog extends MacroBrowserDialog
{
    @Inject
    PageBinder pageBinder;

    public ConfluenceMacroEditor insertMacro()
    {
        clickButton("ok", true);
        return pageBinder.bind(ConfluenceMacroEditor.class);
    }
}
