package com.atlassian.plugin.remotable.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * Fixes JIRA's tab panel descriptor which doesn't pass along moduleFactory correctly so loadClass fails
 */
public class FixedProjectTabPanelModuleDescriptor extends ProjectTabPanelModuleDescriptorImpl
{
    public FixedProjectTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    @Override
    protected void loadClass(Plugin plugin, String clazz) throws PluginParseException
    {
        moduleClass = ProjectTabPanel.class;
    }
}
