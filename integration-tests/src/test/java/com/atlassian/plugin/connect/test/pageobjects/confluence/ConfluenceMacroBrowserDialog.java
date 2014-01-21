package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.confluence.pageobjects.component.dialog.MacroBrowserDialog;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import javax.inject.Inject;

public class ConfluenceMacroBrowserDialog extends MacroBrowserDialog
{
    @Inject
    PageElementFinder pageElementFinder;

    public void clickSave()
    {
        PageElement okButton = pageElementFinder.find(By.className("ok"));
        Poller.waitUntilTrue(okButton.withTimeout(TimeoutType.COMPONENT_LOAD).timed().isVisible());
        okButton.click();
        waitUntilHidden();
    }
}
