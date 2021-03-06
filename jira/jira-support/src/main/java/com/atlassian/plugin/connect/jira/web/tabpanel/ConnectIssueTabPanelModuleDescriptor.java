package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.context.ModuleContextFilter;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.module.ModuleFactory;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 * A ModuleDescriptor for a Connect version of a Jira Issue Tab Panel
 */
public class ConnectIssueTabPanelModuleDescriptor extends IssueTabPanelModuleDescriptorImpl {
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final ModuleContextFilter moduleContextFilter;

    public ConnectIssueTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
                                                ModuleFactory moduleFactory,
                                                IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                                ModuleContextFilter moduleContextFilter) {
        super(authenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public IssueTabPanel3 getModule() {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()));
        return new ConnectIFrameIssueTabPanel(renderStrategy, moduleContextFilter);
    }

    @Override
    public String getModuleClassName() {
        return super.getModuleClassName();
    }

}
