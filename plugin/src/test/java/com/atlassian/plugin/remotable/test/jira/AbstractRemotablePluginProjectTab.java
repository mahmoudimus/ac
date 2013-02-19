package com.atlassian.plugin.remotable.test.jira;

import com.atlassian.jira.pageobjects.pages.project.AbstractProjectTab;
import com.atlassian.plugin.remotable.test.RemotePluginEmbeddedTestPage;
import com.atlassian.pageobjects.PageBinder;

import javax.inject.Inject;

public abstract class AbstractRemotablePluginProjectTab extends AbstractProjectTab
{
    private final String projectTabId;

    @Inject
    PageBinder pageBinder;

    public AbstractRemotablePluginProjectTab(String projectKey, String projectTabId)
    {
        super(projectTabId+"-panel", projectKey);
        this.projectTabId = projectTabId;
    }

    public RemotePluginEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, projectTabId);
    }

}
