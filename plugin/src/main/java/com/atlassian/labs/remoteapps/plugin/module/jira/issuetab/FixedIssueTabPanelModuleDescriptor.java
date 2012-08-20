package com.atlassian.labs.remoteapps.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Fixes JIRA's tab panel descriptor which doesn't pass along moduleFactory correctly so loadClass fails
 */
public class FixedIssueTabPanelModuleDescriptor extends IssueTabPanelModuleDescriptorImpl
{
    public FixedIssueTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    protected void loadClass(Plugin plugin, String clazz) throws PluginParseException
    {
        moduleClass = IssueTabPanel.class;
    }
}
