package com.atlassian.plugin.remotable.test;

import com.atlassian.plugin.remotable.pageobjects.RemoteDialog;
import com.atlassian.plugin.remotable.test.pageobjects.RemotePluginTestPage;

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
