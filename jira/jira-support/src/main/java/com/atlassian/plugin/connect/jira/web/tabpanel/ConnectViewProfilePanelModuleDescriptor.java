package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.web.context.JiraProfileUserContextParameterMapper;
import com.atlassian.plugin.module.ModuleFactory;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 * ModuleDescriptor for Connect version of a ViewProfilePanel
 */
public class ConnectViewProfilePanelModuleDescriptor extends ViewProfilePanelModuleDescriptorImpl
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final PluggableParametersExtractor pluggableParametersExtractor;
    private final JiraProfileUserContextParameterMapper profileUserContextParameterMapper;

    public ConnectViewProfilePanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            PluggableParametersExtractor pluggableParametersExtractor,
            JiraProfileUserContextParameterMapper profileUserContextParameterMapper)
    {
        super(authenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.pluggableParametersExtractor = pluggableParametersExtractor;
        this.profileUserContextParameterMapper = profileUserContextParameterMapper;
    }

    @Override
    public ViewProfilePanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()));
        return new ConnectIFrameProfileTabPanel(renderStrategy, pluggableParametersExtractor, profileUserContextParameterMapper);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}
