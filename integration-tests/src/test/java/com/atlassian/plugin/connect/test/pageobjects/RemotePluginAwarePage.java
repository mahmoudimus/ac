package com.atlassian.plugin.connect.test.pageobjects;

public interface RemotePluginAwarePage
{
    boolean isRemotePluginLinkPresent();

    RemotePluginTestPage clickRemotePluginLink();

    String getRemotePluginLinkHref();
}
