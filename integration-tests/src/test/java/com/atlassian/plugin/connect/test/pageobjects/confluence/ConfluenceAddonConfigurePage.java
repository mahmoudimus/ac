package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.plugin.connect.test.pageobjects.ConnectPageHelper;
import com.atlassian.upm.pageobjects.PluginManager;
import com.google.inject.Inject;

import static com.atlassian.plugin.connect.test.pageobjects.ConnectPageHelper.ConnectPageHelperContainerPage;

/**
 * Confluence Addon configuration page.
 */
public class ConfluenceAddonConfigurePage extends PluginManager implements ConnectPageHelperContainerPage
{
    @Inject
    private ConnectPageHelper connectPageHelper;

    public ConnectPageHelper getConnectPageHelper()
    {
        return connectPageHelper;
    }
}
