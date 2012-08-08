package com.atlassian.labs.remoteapps.test.jira;

import com.atlassian.jira.pageobjects.pages.project.AbstractProjectTab;
import com.atlassian.labs.remoteapps.test.RemoteAppEmbeddedTestPage;
import com.atlassian.pageobjects.PageBinder;

import javax.inject.Inject;

public class JiraRemoteAppProjectTab extends AbstractProjectTab
{
    @Inject
    PageBinder pageBinder;
    public JiraRemoteAppProjectTab(String projectKey)
    {
        super("project-tab-jira-remoteAppProjectTab-panel", projectKey);
    }

    public RemoteAppEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(RemoteAppEmbeddedTestPage.class, "project-tab-jira-remoteAppProjectTab");
    }



}
