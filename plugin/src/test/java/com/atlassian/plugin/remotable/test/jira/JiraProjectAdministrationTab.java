package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
import com.atlassian.jira.pageobjects.project.ProjectConfigHeader;
import com.atlassian.jira.pageobjects.project.ProjectConfigPageTab;
import com.atlassian.jira.pageobjects.project.ProjectConfigTabs;
import com.atlassian.jira.pageobjects.project.ProjectInfoLocator;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Describes a project administration tab.
 */
public class JiraProjectAdministrationTab extends RemotePluginEmbeddedTestPage implements ProjectConfigPageTab
{
    private static final String TAB_ID = "servlet-jira-remotePluginProjectConfigTab";

    @Inject
    private PageBinder pageBinder;

    @ElementBy (id = "project-config-actions")
    private PageElement operations;

    private ProjectInfoLocator projectInfoLocator;
    private final String projectKey;

    public JiraProjectAdministrationTab()
    {
        super(TAB_ID);
        projectKey = null;
    }

    public JiraProjectAdministrationTab(final String projectKey)
    {
        super(TAB_ID);
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
    public ProjectConfigActions openOperations()
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
