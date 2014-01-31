package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.pageobjects.pages.ViewProfilePage;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;

/**
 * An user ViewProfile page that is expected to have a panel provided by a remote plugin.
 */
public class JiraViewProfilePage extends ViewProfilePage
{
    final private String profileUsername;

    public JiraViewProfilePage(String profileUsername)
    {
        this.profileUsername = profileUsername;
    }

    @Override
    public String getUrl()
    {
        return super.getUrl() + "?name=" + profileUsername;
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }

    public RemoteWebPanel findWebPanelFromXMLAddOn(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId, "remote-web-panel-");
    }
}
