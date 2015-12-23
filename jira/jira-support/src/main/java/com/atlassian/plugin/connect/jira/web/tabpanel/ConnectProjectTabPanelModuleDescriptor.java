package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.module.ModuleFactory;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 * ModuleDescriptor for Connect project of a ProjectTabPanel
 */
public class ConnectProjectTabPanelModuleDescriptor extends ProjectTabPanelModuleDescriptorImpl
{

    private IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private PluggableParametersExtractor pluggableParametersExtractor;

    public ConnectProjectTabPanelModuleDescriptor(JiraAuthenticationContext jiraAuthenticationContext,
            ModuleFactory moduleFactory, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            PluggableParametersExtractor pluggableParametersExtractor)
    {
        super(jiraAuthenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.pluggableParametersExtractor = pluggableParametersExtractor;
    }

    @Override
    public ProjectTabPanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()));
        return new ConnectIFrameProjectTabPanel(renderStrategy, pluggableParametersExtractor);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
