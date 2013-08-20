package com.atlassian.plugin.connect.test.pageobjects;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.plugin.module.page.RemotePageDescriptorCreator;

public class MessagePage extends RemotePluginEmbeddedTestPage implements Page
{
    private final String appKey;
    private final String pageKey;

    public MessagePage(String appKey, String pageKey)
    {
        super("servlet-" + pageKey);
        this.appKey = appKey;
        this.pageKey = pageKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet" + RemotePageDescriptorCreator.createLocalUrl(appKey, pageKey);
    }
}
