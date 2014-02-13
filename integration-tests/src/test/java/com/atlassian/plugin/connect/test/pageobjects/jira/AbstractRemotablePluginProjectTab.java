package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.jira.pageobjects.pages.project.AbstractProjectTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;

public abstract class AbstractRemotablePluginProjectTab extends AbstractProjectTab
{
    private final String moduleKey;

    @Inject
    PageBinder pageBinder;

    public AbstractRemotablePluginProjectTab(String projectKey, String pluginKey, String moduleKey)
    {
        super(pluginKey + ":" + moduleKey + "-panel", projectKey);
        this.moduleKey = moduleKey;
    }

    public RemotePluginEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, moduleKey);
    }

}
