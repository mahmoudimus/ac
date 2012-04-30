package com.atlassian.labs.remoteapps.test;

import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemoteAppDialog
{
    @Inject
    private AtlassianWebDriver driver;

    // The Remote App iFrame page embedded within the dialog.
    private final RemoteAppTestPage embeddedPage;

    @FindBy(className = "ra-dialog-submit")
    private WebElement submitButton;

    @FindBy(className = "ra-dialog-cancel")
    private WebElement cancelButton;

    public RemoteAppDialog(RemoteAppTestPage embeddedPage)
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
        return !driver.elementIsVisible(By.className("ra-dialog-content"));
    }

    public void cancel()
    {
        cancelButton.click();
    }

}
