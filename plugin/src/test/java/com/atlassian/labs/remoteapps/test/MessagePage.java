package com.atlassian.labs.remoteapps.test;

import com.atlassian.pageobjects.Page;

public class MessagePage extends RemoteAppEmbeddedTestPage implements Page
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
        return "/plugins/servlet/remoteapps/" + appKey + "/" + pageKey;
    }
}
