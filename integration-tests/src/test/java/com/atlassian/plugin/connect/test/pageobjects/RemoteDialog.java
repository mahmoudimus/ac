package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemoteDialog extends AbstractConnectIFrameComponent<RemoteDialog>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @FindBy(className = "ap-dialog-submit")
    protected WebElement submitButton;

    @FindBy(className = "ap-dialog-cancel")
    protected WebElement cancelButton;

    private static final String DIALOG_CONTAINER = "ap-dialog-container";

    protected String getFrameId()
    {
        return elementFinder.find(By.cssSelector("." + DIALOG_CONTAINER + " iframe")).getAttribute("id");
    }

    /**
     * Hits the "Submit" button on the dialog. Returns true if the dialog was dismissed. Returns false if the dialog is still
     * visible (this may be a valid scenario, if the embedded iframe in the dialog has forcefully cancelled the user's
     * attempt to close the dialog).
     */
    public boolean submit()
    {
        submitButton.click();
        return isDialogClosed();
    }

    /**
     * Hits the "Cancel" button on the dialog. Returns true if the dialog was dismissed. Returns false if the dialog is still
     * visible.
     */
    public boolean cancel()
    {
        cancelButton.click();
        return isDialogClosed();
    }

    private boolean isDialogClosed()
    {
        final By dialogContentLocator = By.className(DIALOG_CONTAINER);
        try
        {
            return !driver.elementIsVisible(dialogContentLocator);
        }
        catch (StaleElementReferenceException e)
        {
            if (!driver.elementExists(dialogContentLocator))
            {
                return true;
            }
            else
            {
                logger.debug("We got a 'StaleElementReferenceException' and yet the element still appears to be existing, not sure what's going on here. Rethrowing.");
                throw e;
            }
        }
    }
}
