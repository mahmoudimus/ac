package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.google.common.base.Optional;

import javax.inject.Inject;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.ID;

/**
 * Represents any type of addon that has a link (web item) and content (servlet) Includes the tab panels and pages
 */
public class LinkedRemoteContent
{
    private RemoteWebItem webItem;
    private final String pageKey;
    private final ItemMatchingMode mode;
    private final String matchValue;
    private final Option<String> dropDownLinkId;
    private final String extraPrefix;

    @Inject
    private PageBinder pageBinder;

    public LinkedRemoteContent(ItemMatchingMode mode, String id, Option<String> dropDownLinkId, String pageKey)
    {
        this(mode, id, dropDownLinkId, pageKey, "");
    }

    public LinkedRemoteContent(ItemMatchingMode mode, String id, Option<String> dropDownLinkId, String pageKey, String extraPrefix)
    {
        this.mode = mode;
        this.matchValue = id;
        this.dropDownLinkId = dropDownLinkId;
        this.pageKey = pageKey;
        this.extraPrefix = extraPrefix;
    }

    public LinkedRemoteContent(String matchValue, Option<String> dropDownLinkId, String pageKey)
    {
        this(ID, matchValue, dropDownLinkId, pageKey);
    }

    @Init
    public void init()
    {
        webItem = pageBinder.bind(RemoteWebItem.class, mode, matchValue, Optional.fromNullable(dropDownLinkId.getOrNull())); // Doh!
    }

    public RemoteWebItem getWebItem()
    {
        return webItem;
    }

    public ConnectAddOnEmbeddedTestPage click()
    {
        webItem.click();

        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, pageKey, extraPrefix);
    }
}
