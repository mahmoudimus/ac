package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
import com.atlassian.jira.pageobjects.project.ProjectConfigHeader;
import com.atlassian.jira.pageobjects.project.ProjectConfigPageTab;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.ProjectInfoLocator;
import com.atlassian.jira.pageobjects.project.ProjectSettingsHeader;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonEmbeddedTestPage;

import javax.inject.Inject;

/**
 * Describes a project administration tab.
 */
public class JiraProjectAdministrationTab extends ConnectAddonEmbeddedTestPage implements ProjectConfigPageTab
{
    @Inject
    private PageBinder pageBinder;

    @ElementBy(id = "project-config-actions")
    private PageElement operations;

    private ProjectInfoLocator projectInfoLocator;
    private final String projectKey;

    public JiraProjectAdministrationTab(String projectKey, String addonKey, String moduleKey)
    {
        super(addonKey, moduleKey, true);
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
    public ProjectSettingsHeader getProjectSettingsHeader() {
        return pageBinder.bind(ProjectSettingsHeader.class);
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
