package com.atlassian.connect.test.jira.pageobjects;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.common.pageobjects.RemoteWebPanel;

/**
 * Describes a project administration page.
 */
public class JiraProjectAdministrationPage implements Page
{
    private final String projectKey;

    @Inject
    private PageBinder pageBinder;

    public JiraProjectAdministrationPage(String projectKey)
    {
        this.projectKey = projectKey;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey;
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId);
    }
}

