package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.pages.project.AbstractProjectTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;

public abstract class AbstractRemotablePluginProjectTab extends AbstractProjectTab
{
    private final String projectTabId;

    @Inject
    PageBinder pageBinder;

    public AbstractRemotablePluginProjectTab(String projectKey, String projectTabId)
    {
        super(projectTabId + "-panel", projectKey);
        this.projectTabId = projectTabId;
    }

    public RemotePluginEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, projectTabId);
    }

}
