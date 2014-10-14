package com.atlassian.plugin.connect.test.pageobjects;

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

    public boolean wasSubmitted()
    {
        return Boolean.valueOf(embeddedConnectPage.getValue("submitted"));
    }

    public String getValueById(String id)
    {
        return embeddedConnectPage.getValueById(id);
    }
}
