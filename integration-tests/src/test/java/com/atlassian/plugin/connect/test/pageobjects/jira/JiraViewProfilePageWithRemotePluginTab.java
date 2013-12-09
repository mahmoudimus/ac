package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;

public class JiraViewProfilePageWithRemotePluginTab extends RemotePluginEmbeddedTestPage implements Page
{
    private static final String DEFAULT_PAGE_KEY = "profile-tab-profile-tab-panel";
    private final String profileKey;

    public JiraViewProfilePageWithRemotePluginTab(String profileKey)
    {
        this(DEFAULT_PAGE_KEY, profileKey);
    }

    public JiraViewProfilePageWithRemotePluginTab(String pageKey, String profileKey)
    {
        super(pageKey);
        this.profileKey = profileKey;
    }

    @Override
    public String getUrl()
    {
        return "/ViewProfile.jspa?name=" + profileKey;
    }


}
