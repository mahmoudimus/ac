package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.fugue.Option;
import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;
import com.google.common.base.Optional;

/**
 * An user ViewProfile page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewProfilePage extends ViewProfilePage
{
    final private Option<String> profileUsername;

    public JiraViewProfilePage(Option<String> profileUsername)
    {
        this.profileUsername = profileUsername;
    }

    @Deprecated // use ctr w option
    public JiraViewProfilePage(String profileUsername)
    {
        this(Option.some(profileUsername));
    }

    @Override
    public String getUrl()
    {
        return profileUsername.isEmpty() ? super.getUrl() : super.getUrl() + "?name=" + profileUsername.get();
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteWebItem findWebItem(String webItemId, Optional<String> dropDownMenuId)
    {
        return pageBinder.bind(RemoteWebItem.class, webItemId, dropDownMenuId);
    }

    public LinkedRemoteContent findTabPanel(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, pageKey);
    }

    public LinkedRemoteContent findConnectPage(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return findRemoteLinkedContent(webItemId, dropDownMenuId, "servlet-" + pageKey);
    }

    private LinkedRemoteContent findRemoteLinkedContent(String webItemId, Option<String> dropDownMenuId, String pageKey)
    {
        return pageBinder.bind(LinkedRemoteContent.class, webItemId, dropDownMenuId, pageKey);
    }

}
