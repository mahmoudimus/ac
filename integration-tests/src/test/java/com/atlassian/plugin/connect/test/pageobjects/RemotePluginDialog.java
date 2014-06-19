package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

/**
 * Describes a <dialog-page> Remote Module - must be bound after the dialog has been opened.
 */
public class RemotePluginDialog extends RemoteDialog
{
    // The Remotable Plugin iFrame page embedded within the dialog.
    @XmlDescriptor // migrate to the new embeddedConnectPage data member
    private final RemotePluginTestPage embeddedPage;

    private final ConnectAddOnTestPage embeddedConnectPage;

    public RemotePluginDialog(RemotePluginTestPage embeddedPage)
    {
        this.embeddedPage = embeddedPage;
        this.embeddedConnectPage = null;
    }

    public RemotePluginDialog(ConnectAddOnTestPage embeddedConnectPage)
    {
        this.embeddedPage = null;
        this.embeddedConnectPage = embeddedConnectPage;
    }

    public boolean wasSubmitted()
    {
        return Boolean.valueOf(null == embeddedConnectPage ? embeddedPage.getValue("submitted") : embeddedConnectPage.getValue("submitted"));
    }

    public String getValueById(String id)
    {
        return null == embeddedConnectPage ? embeddedPage.getValueById(id) : embeddedConnectPage.getValueById(id);
    }
}
