package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;

/**
 * Created by IntelliJ IDEA. User: mrdon Date: 18/02/12 Time: 1:18 AM To change this template use
 * File | Settings | File Templates.
 */
public interface RemotePluginAwarePage extends Page
{
    boolean isRemotePluginLinkPresent();

    RemotePluginTestPage clickRemotePluginLink();
}
