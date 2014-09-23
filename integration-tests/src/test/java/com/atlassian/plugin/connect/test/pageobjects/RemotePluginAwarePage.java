package com.atlassian.plugin.connect.test.pageobjects;

public interface RemotePluginAwarePage
{
    boolean isRemotePluginLinkPresent();

    ConnectAddOnEmbeddedTestPage clickAddOnLink();
}
