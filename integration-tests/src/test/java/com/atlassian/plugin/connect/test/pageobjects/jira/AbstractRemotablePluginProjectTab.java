package com.atlassian.plugin.connect.test.pageobjects.jira;

import javax.inject.Inject;

import com.atlassian.jira.projects.pageobjects.page.legacy.AbstractProjectTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.test.pageobjects.RemotePluginEmbeddedTestPage;
import com.atlassian.plugin.connect.test.utils.WebItemUtils;

public abstract class AbstractRemotablePluginProjectTab extends AbstractProjectTab
{
    private final String moduleKey;

    @Inject
    PageBinder pageBinder;

    public AbstractRemotablePluginProjectTab(String projectKey, String pluginKey, String moduleKey)
    {
        super(WebItemUtils.linkId(pluginKey, moduleKey) + "-panel", projectKey);
        this.moduleKey = moduleKey;
    }

    public RemotePluginEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(RemotePluginEmbeddedTestPage.class, moduleKey);
    }

}
