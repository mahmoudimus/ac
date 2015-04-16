package com.atlassian.plugin.connect.test.pageobjects;

import com.google.common.base.Function;
import org.openqa.selenium.WebDriver;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemotePluginDialog extends RemoteDialog
{
    private final ConnectAddOnEmbeddedTestPage embeddedConnectPage;

    public RemotePluginDialog(ConnectAddOnEmbeddedTestPage embeddedConnectPage)
    {
        this(embeddedConnectPage, false); // default to "not inline dialog"
    }

    public RemotePluginDialog(ConnectAddOnEmbeddedTestPage embeddedConnectPage, final boolean isInlineDialog)
    {
        super(isInlineDialog);
        this.embeddedConnectPage = embeddedConnectPage;
    }

    /**
     * Hits the "Submit" button on the dialog, but expects the embedded iframe in the dialog to cancel the user's
     * attempt to close the dialog.
     */
    public void submitAndWaitUntilSubmitted()
    {
        submitButton.click();
        poller.waitUntil(new Function<WebDriver, Boolean>()
        {
            @Override
            public Boolean apply(WebDriver webDriver)
            {
                return wasSubmitted();
            }
        });
    }

    public boolean wasSubmitted()
    {
        return Boolean.valueOf(embeddedConnectPage.getValue("submitted"));
    }

    public String getValueById(String id)
    {
        return embeddedConnectPage.getValueById(id);
    }
}
