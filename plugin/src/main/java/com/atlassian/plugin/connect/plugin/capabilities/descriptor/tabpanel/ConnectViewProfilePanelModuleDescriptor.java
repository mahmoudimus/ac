package com.atlassian.plugin.connect.plugin.capabilities.descriptor.tabpanel;

import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectModuleDescriptor;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.tabpanel.profile.ConnectIFrameProfileTabPanel;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.user.UserManager;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator.moduleKeyOnly;

/**
 * ModuleDescriptor for Connect version of a ViewProfilePanel
 */
public class ConnectViewProfilePanelModuleDescriptor extends ViewProfilePanelModuleDescriptorImpl implements ConnectModuleDescriptor<ViewProfilePanel>
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;
    private final UserManager userManager;
    private String addonKey;

    public ConnectViewProfilePanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            ModuleContextFilter moduleContextFilter,
            UserManager userManager)
    {
        super(authenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.moduleContextFilter = moduleContextFilter;
        this.userManager = userManager;
    }

    @Override
    public ViewProfilePanel getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKey, moduleKeyOnly(getKey()));
        return new ConnectIFrameProfileTabPanel(renderStrategy, moduleContextFilter, userManager);
    }

    @Override
    public void setAddonKey(String addonKey)
    {
        this.addonKey = addonKey;
    }
}
