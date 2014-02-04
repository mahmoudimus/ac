package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemoteWebPanel;

/**
 * Describes a project administration page.
 */
public class JiraProjectAdministrationPage implements Page
{
    private final String projectKey;
    private final String extraPrefix;

    @Inject
    private PageBinder pageBinder;

    public JiraProjectAdministrationPage(String projectKey)
    {
        this(projectKey,"");
    }

    public JiraProjectAdministrationPage(String projectKey, String extraPrefix)
    {
        this.projectKey = projectKey;
        this.extraPrefix = extraPrefix;
    }

    @Override
    public String getUrl()
    {
        return "/plugins/servlet/project-config/" + projectKey;
    }

    public RemoteWebPanel findWebPanel(String panelId)
    {
        return pageBinder.bind(RemoteWebPanel.class, panelId, extraPrefix);
    }
}

