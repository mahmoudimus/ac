package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;

public class MessagePage extends RemotePluginEmbeddedTestPage implements Page
{
    private final String appKey;
    private final String pageKey;
    private final String extraPrefix;

    public MessagePage(String appKey, String pageKey)
    {
        this(appKey, pageKey, "");
    }

    public MessagePage(String appKey, String pageKey, String extraPrefix)
    {
        super(pageKey, extraPrefix);
        this.appKey = appKey;
        this.pageKey = pageKey;
        this.extraPrefix = extraPrefix;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet" + RemotePageDescriptorCreator.createLocalUrl(appKey, pageKey);
    }
}
