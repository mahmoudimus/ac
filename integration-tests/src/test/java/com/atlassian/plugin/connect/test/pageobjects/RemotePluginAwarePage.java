package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

public interface RemotePluginAwarePage
{
    boolean isRemotePluginLinkPresent();

    @XmlDescriptor
    RemotePluginTestPage clickRemotePluginLink();

    ConnectAddOnEmbeddedTestPage clickAddOnLink();

    String getRemotePluginLinkHref();
}
