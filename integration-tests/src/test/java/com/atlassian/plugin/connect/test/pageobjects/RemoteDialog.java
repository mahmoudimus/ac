package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.confluence.pageobjects.component.dialog.AbstractDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.atlassian.webdriver.utils.element.ElementConditions;
import com.atlassian.webdriver.utils.element.WebDriverPoller;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
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

    @ElementBy(className = "aui-blanket")
    protected PageElement auiBlanket;

    @ElementBy(className = "ap-dialog-submit")
    protected PageElement submitButton;

    @ElementBy(className = "ap-dialog-cancel")
    protected PageElement cancelButton;

    @ElementBy(className = "aui-dialog2-header-main")
    protected PageElement titleElement;

    @Inject
    protected WebDriverPoller poller;

    @Inject
    protected Timeouts timeouts;

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
        try
        {
            return getFrameIdUnsafe();
        }
        catch (StaleElementReferenceException e)
        {
            // JavaScript code can recreate the iframe while the test is clicking and hovering,
            // and webdriver complains if we are unlucky enough to find the iframe dom element before
            // the re-creation but ask for its id after the re-creation
            return getFrameIdUnsafe();
        }
    }

    private String getFrameIdUnsafe()
    {
        final String cssClass = isInlineDialog ? INLINE_DIALOG_CONTAINER : DIALOG_CONTAINER;
        return elementFinder.find(By.cssSelector("." + cssClass + " iframe")).getAttribute("id");
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

    public String getTitle() {
        return titleElement.getText();
    }

    public void waitUntilHidden() {
        poller.waitUntil(ElementConditions.isNotPresent(By.className(DIALOG_CONTAINER)), 10);
        this.waitUntilAUIBlanketHidden();
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
