package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.confluence.webdriver.pageobjects.component.dialog.AbstractDialog;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import org.openqa.selenium.NoSuchElementException;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemoteDialog extends AbstractRemoteDialog<RemoteDialog>
{

    @ElementBy(className = "aui-blanket")
    protected PageElement auiBlanket;

    @ElementBy(className = "ap-dialog-submit")
    protected PageElement submitButton;

    @ElementBy(className = "ap-dialog-cancel")
    protected PageElement cancelButton;

    @ElementBy(className = "aui-dialog2-header-main")
    protected PageElement titleElement;

    private static final String DIALOG_CONTAINER_CLASS = "ap-dialog-container";

    @Override
    protected String getContainerCssClassName()
    {
        return DIALOG_CONTAINER_CLASS;
    }

    public void waitUntilHidden() {
        super.waitUntilHidden();
        this.waitUntilAUIBlanketHidden();
    }

    public void submitAndWaitUntilHidden()
    {
        submitButton.click();
        waitUntilHidden();
    }

    public void cancelAndWaitUntilHidden()
    {
        cancelButton.click();
        waitUntilHidden();
    }

    public boolean hasChrome()
    {
    	try 
    	{
    		return submitButton != null && submitButton.isVisible();
    	}
    	catch (NoSuchElementException e)
    	{
    		return false;
    	}
    }

    public String getTitle()
    {
        return titleElement.getText();
    }

    /**
     * @see AbstractDialog#waitUntilAUIBlanketHidden()
     */
    protected void waitUntilAUIBlanketHidden() {
        Poller.waitUntilFalse("Blanket should be hidden after closing the dialog", this.auiBlanket.timed().isVisible());

        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
