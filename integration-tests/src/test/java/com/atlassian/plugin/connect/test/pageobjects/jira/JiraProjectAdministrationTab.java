package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.projectconfig.pageobjects.ProjectConfigActions;
import com.atlassian.jira.projectconfig.pageobjects.ProjectConfigHeader;
import com.atlassian.jira.projectconfig.pageobjects.ProjectConfigPageTab;
import com.atlassian.jira.projectconfig.pageobjects.ProjectConfigTabs;
import com.atlassian.jira.projectconfig.pageobjects.ProjectInfoLocator;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;

import javax.inject.Inject;

/**
 * Describes a project administration tab.
 */
public class JiraProjectAdministrationTab extends RemotePluginEmbeddedTestPage implements ProjectConfigPageTab
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "project-config-actions")
    private PageElement operations;

    private ProjectInfoLocator projectInfoLocator;
    private final String projectKey;

    public JiraProjectAdministrationTab(String projectKey, String moduleKey)
    {
        this(projectKey, moduleKey, "");
    }

    public JiraProjectAdministrationTab(String projectKey, String moduleKey, String extraPrefix)
    {
        super(moduleKey, extraPrefix);
        this.projectKey = projectKey;
    }

    @Init
    public void init()
    {
        projectInfoLocator = pageBinder.bind(ProjectInfoLocator.class);
    }


    @Override
    public String getProjectKey()
    {
        return projectKey;
    }

    @Override
    public long getProjectId()
    {
        return projectInfoLocator.getProjectId();
    }

    @Override
    public ProjectConfigTabs getTabs()
    {
        return pageBinder.bind(ProjectConfigTabs.class);
    }

    @Override
    public ProjectConfigHeader getProjectHeader()
    {
        return pageBinder.bind(ProjectConfigHeader.class);
    }

    @Override
    public ProjectConfigActions getOperations()
    {
        operations.click();
        return pageBinder.bind(ProjectConfigActions.class);
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
