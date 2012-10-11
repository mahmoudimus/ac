package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.project.AbstractProjectTab;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.pageobjects.PageBinder;

import javax.inject.Inject;

public class JiraRemotablePluginProjectTab extends AbstractProjectTab
{
    @Inject
    PageBinder pageBinder;
    public JiraRemotablePluginProjectTab(String projectKey)
    {
        super("project-tab-jira-remotePluginProjectTab-panel", projectKey);
    }

    public RemotePluginEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, "project-tab-jira-remotePluginProjectTab");
    }



}
