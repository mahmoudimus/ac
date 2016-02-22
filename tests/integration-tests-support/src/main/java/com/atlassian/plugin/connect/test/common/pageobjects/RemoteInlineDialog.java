package com.atlassian.plugin.connect.test.common.pageobjects;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.utils.element.ElementConditions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Describes a <tt>webitem</tt> inline dialog - must be bound after the inline dialog has been opened.
 */
public class RemoteInlineDialog extends AbstractRemoteDialog<RemoteInlineDialog>
{

    private static final String INLINE_DIALOG_CONTAINER_CLASS = "ap-container";

    private static final String INLINE_DIALOG_HIDE_BUTTON_ID = "inline-dialog-hide-button";

    @Override
    protected String getContainerCssClassName()
    {
        return INLINE_DIALOG_CONTAINER_CLASS;
    }

    @WaitUntil
    protected void waitUntilHideButtonLoaded()
    {
        waitUntilContentElementNotEmpty(INLINE_DIALOG_HIDE_BUTTON_ID);
    }

    protected ExpectedCondition getHiddenCondition(By locator)
    {
        return ElementConditions.isNotVisible(locator);
    }

    public void hideAndWaitUntilHidden()
    {
        withinIFrame(driver1 -> {
            WebElement button = driver1.findElement(By.id(INLINE_DIALOG_HIDE_BUTTON_ID));
            button.click();
            return null;
        });
        waitUntilHidden();
    }
}