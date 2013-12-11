package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.ConnectPageOperations;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.google.common.base.Optional;
import com.google.inject.Inject;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode;

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

    @Deprecated // use it.ConnectWebDriverTestBase#connectPageOperations
    public LinkedRemoteContent findConnectPage(ItemMatchingMode mode, String linkText, Option<String> dropDownMenuId,
                                               String pageKey)
    {
        return connectPageOperations.findConnectPage(mode, linkText, dropDownMenuId, pageKey);
    }

    @Deprecated // use it.ConnectWebDriverTestBase#connectPageOperations
    public LinkedRemoteContent findTabPanel(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return connectPageOperations.findTabPanel(webItemId, dropDownMenuId, pageKey);
    }

    @Deprecated // use it.ConnectWebDriverTestBase#connectPageOperations
    public LinkedRemoteContent findConnectPage(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return connectPageOperations.findConnectPage(webItemId, dropDownMenuId, pageKey);
    }
}
