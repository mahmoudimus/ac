package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.plugin.connect.test.pageobjects.RemoteDialog;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginTestPage;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemotePluginDialog extends RemoteDialog
{
    // The Remotable Plugin iFrame page embedded within the dialog.
    private final RemotePluginTestPage embeddedPage;

    public RemotePluginDialog(RemotePluginTestPage embeddedPage)
    {
        this.embeddedPage = embeddedPage;
    }

    public boolean wasSubmitted()
    {
        return Boolean.valueOf(embeddedPage.getValue("submitted"));
    }
}
