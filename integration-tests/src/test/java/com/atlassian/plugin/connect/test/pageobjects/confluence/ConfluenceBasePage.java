package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.google.common.base.Optional;
import com.google.inject.Inject;

/**
 * Base confluence page.
 */
public abstract class ConfluenceBasePage implements Page
{
    @Inject
    private ConnectPageOperations connectPageOperations;

    @Deprecated // use it.ConnectWebDriverTestBase#connectPageOperations
    public RemoteWebPanel findWebPanel(String id)
    {
        return connectPageOperations.findWebPanel(id);
    }

    @Deprecated // use it.ConnectWebDriverTestBase#connectPageOperations
    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownLinkId)
    {
        return connectPageOperations.findWebItem(webItemId, dropDownLinkId);
    }

    @Deprecated // use it.ConnectWebDriverTestBase#connectPageOperations
    public Boolean webItemDoesNotExist(String webItemId)
    {
        return connectPageOperations.webItemDoesNotExist(webItemId);
    }

}
