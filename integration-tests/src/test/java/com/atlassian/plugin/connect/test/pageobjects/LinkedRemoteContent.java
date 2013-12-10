package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.google.common.base.Optional;

import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.ID;

/**
 * Represents any type of addon that has a link (web item) and content (servlet)
 * Includes the tab panels and pages
 */
public class LinkedRemoteContent
{
    private  RemoteWebItem webItem;
    private final String pageKey;
    private final ItemMatchingMode mode;
    private final String matchValue;
    private final Option<String> dropDownLinkId;

    @Inject
    private PageBinder pageBinder;

    public LinkedRemoteContent(ItemMatchingMode mode, String id, Option<String> dropDownLinkId, String pageKey)
    {
        this.mode = mode;
        this.matchValue = id;
        this.dropDownLinkId = dropDownLinkId;
        this.pageKey = pageKey;
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

    public RemotePluginEmbeddedTestPage click()
    {
        webItem.click();

        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, "servlet-" + pageKey);
    }
}
