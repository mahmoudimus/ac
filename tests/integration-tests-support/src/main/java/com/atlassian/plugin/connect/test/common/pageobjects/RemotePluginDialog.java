package com.atlassian.plugin.connect.test.common.pageobjects;

import com.google.common.base.Function;
import org.openqa.selenium.WebDriver;

/**
 * Describes a <tt>dialog-page</tt> Remote Module - must be bound after the dialog has been opened.
 */
public class RemotePluginDialog extends RemoteDialog
{
    private final ConnectAddOnEmbeddedTestPage embeddedConnectPage;

    public RemotePluginDialog(ConnectAddOnEmbeddedTestPage embeddedConnectPage)
    {
        super(embeddedConnectPage.getIFrame());
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
