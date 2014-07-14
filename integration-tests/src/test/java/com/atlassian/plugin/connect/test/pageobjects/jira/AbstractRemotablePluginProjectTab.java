package com.atlassian.plugin.connect.test.pageobjects.jira;

import com.atlassian.jira.projects.pageobjects.page.legacy.AbstractProjectTab;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.utils.WebItemUtils;

import javax.inject.Inject;

public abstract class AbstractRemotablePluginProjectTab extends AbstractProjectTab
{
    private final String addOnKey;
    private final String moduleKey;

    @Inject
    PageBinder pageBinder;

    public AbstractRemotablePluginProjectTab(String projectKey, String addOnKey, String moduleKey)
    {
        super(WebItemUtils.linkId(ConnectPluginInfo.getPluginKey(), ModuleKeyUtils.addonAndModuleKey(addOnKey, moduleKey)) + "-panel", projectKey);
        this.addOnKey = addOnKey;
        this.moduleKey = moduleKey;
    }

    public ConnectAddOnEmbeddedTestPage getEmbeddedPage()
    {
        return pageBinder.bind(ConnectAddOnEmbeddedTestPage.class, addOnKey, moduleKey, true);
    }
}
