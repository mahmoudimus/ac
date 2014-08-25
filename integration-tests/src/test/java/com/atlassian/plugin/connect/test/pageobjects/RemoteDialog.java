package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

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

    @FindBy(className = "aui-dialog2-header-main")
    protected WebElement titleElement;

    private static final String DIALOG_CONTAINER = "ap-dialog-container";
    private static final String INLINE_DIALOG_CONTAINER = "ap-container";

    private final boolean isInlineDialog;

    public RemoteDialog()
    {
        this(false); // default to "not inline"
    }

    public RemoteDialog(boolean isInlineDialog)
    {
        this.isInlineDialog = isInlineDialog;
    }

    protected String getFrameId()
    {
        final String cssClass = isInlineDialog ? INLINE_DIALOG_CONTAINER : DIALOG_CONTAINER;
        return elementFinder.find(By.cssSelector("." + cssClass + " iframe")).getAttribute("id");
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

    public boolean hasChrome()
    {
    	try 
    	{
    		return submitButton != null && submitButton.isDisplayed();
    	}
    	catch (NoSuchElementException e)
    	{
    		return false;
    	}
    }

    public String getTitle() {
        return titleElement.getText();
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
