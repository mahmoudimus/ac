package com.atlassian.plugin.connect.test.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.google.common.base.Optional;

public class RemoteTabPanel
{
    private  RemoteWebItem webItem;
    private final String pageKey;
    private final String id;
    private final Optional<String> dropDownLinkId;

    @Inject
    private PageBinder pageBinder;

//    public RemoteTabPanel(RemoteWebItem webItem, String pageKey)
//    {
//        this.webItem = webItem;
//        this.pageKey = pageKey;
//    }

    public RemoteTabPanel(String id, Optional<String> dropDownLinkId, String pageKey)
    {
//        this(new RemoteWebItem(id, dropDownLinkId), pageKey);

        this.id = id;
        this.dropDownLinkId = dropDownLinkId;
        this.pageKey = pageKey;
    }

    @Init
    public void init()
    {
        webItem = pageBinder.bind(RemoteWebItem.class, id, dropDownLinkId);
    }

    public RemoteWebItem getWebItem()
    {
        return webItem;
    }

    public RemotePluginEmbeddedTestPage click()
    {
        webItem.click();
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, pageKey);
    }
}
