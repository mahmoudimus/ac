package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;

/**
 * Describes a project administration page.
 */
public class JiraProjectAdministrationPanel extends RemotePluginEmbeddedTestPage implements Page
{
    private final String projectKey;

    public JiraProjectAdministrationPanel(final String pageKey, final String projectKey)
    {
        super(pageKey);
        this.projectKey = projectKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey;
    }
}

