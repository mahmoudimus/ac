package com.atlassian.plugin.connect.test.common.pageobjects;

import javax.inject.Inject;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebItem.ItemMatchingMode;

import com.google.common.base.Optional;


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
        this(ItemMatchingMode.ID, matchValue, dropDownLinkId, pageKey);
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
        return click(ConnectAddOnEmbeddedTestPage.class, extraPrefix, pageKey, true);
    }

    public <T> T click(Class<T> type, Object... args)
    {
        webItem.click();

        return pageBinder.bind(type, args);
    }
}
