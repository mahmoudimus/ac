package com.atlassian.plugin.remotable.test;

import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemotePluginDialog
{
    @Inject
    private AtlassianWebDriver driver;

    // The Remotable Plugin iFrame page embedded within the dialog.
    private final RemotePluginTestPage embeddedPage;

    @FindBy(className = "ap-dialog-submit")
    private WebElement submitButton;

    @FindBy(className = "ap-dialog-cancel")
    private WebElement cancelButton;

    public RemotePluginDialog(RemotePluginTestPage embeddedPage)
    {
        this.embeddedPage = embeddedPage;
    }

    public boolean wasSubmitted()
    {
        return Boolean.valueOf(embeddedPage.getValue("submitted"));
    }

    /**
     * Hits the "Submit" button on the dialog. Returns true if the dialog was dismissed. Returns false if the dialog is still
     * visible (this may be a valid scenario, if the embedded iframe in the dialog has forcefully cancelled the user's
     * attempt to close the dialog).
     */
    public boolean submit()
    {
        submitButton.click();
        return !driver.elementIsVisible(By.className("ap-dialog-content"));
    }

    public void cancel()
    {
        cancelButton.click();
    }

}
