package com.atlassian.plugin.connect.test.common.pageobjects;

import com.atlassian.pageobjects.elements.PageElement;

public interface RemotePluginAwarePage
{
    ConnectAddOnEmbeddedTestPage clickAddOnLink();

    PageElement findLinkElement();
}
