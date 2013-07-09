package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.remotable.test.RemoteWebPanel;

/**
 * Describes a project administration page.
 */
public class JiraProjectAdministrationPanelPage extends RemotePluginEmbeddedTestPage implements Page
{
    private final String projectKey;

    public JiraProjectAdministrationPanelPage(final String pageKey, final String projectKey)
    {
        super(pageKey);
        this.projectKey = projectKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey;
    }

    public RemoteWebPanel getRemoteWebPanel()
    {
        return new RemoteWebPanel(this);
    }
}

