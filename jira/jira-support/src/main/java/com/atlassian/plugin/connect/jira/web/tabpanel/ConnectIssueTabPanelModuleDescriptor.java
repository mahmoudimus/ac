package com.atlassian.plugin.connect.jira.web.tabpanel;

import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.PluggableParametersExtractor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.jira.web.context.IssueContextParameterMapper;
import com.atlassian.plugin.connect.jira.web.context.ProjectContextParameterMapper;
import com.atlassian.plugin.module.ModuleFactory;

import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.addonKeyOnly;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.moduleKeyOnly;

/**
 * A ModuleDescriptor for a Connect version of a Jira Issue Tab Panel
 */
public class ConnectIssueTabPanelModuleDescriptor extends IssueTabPanelModuleDescriptorImpl
{

    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final PluggableParametersExtractor pluggableParametersExtractor;
    private final IssueContextParameterMapper issueContextParameterMapper;
    private final ProjectContextParameterMapper projectContextParameterMapper;

    public ConnectIssueTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            PluggableParametersExtractor pluggableParametersExtractor,
            IssueContextParameterMapper issueContextParameterMapper,
            ProjectContextParameterMapper projectContextParameterMapper)
    {
        super(authenticationContext, moduleFactory);
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.pluggableParametersExtractor = pluggableParametersExtractor;
        this.issueContextParameterMapper = issueContextParameterMapper;
        this.projectContextParameterMapper = projectContextParameterMapper;
    }

    @Override
    public IssueTabPanel3 getModule()
    {
        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyRegistry.getOrThrow(addonKeyOnly(getKey()), moduleKeyOnly(getKey()));
        return new ConnectIFrameIssueTabPanel(renderStrategy, pluggableParametersExtractor, issueContextParameterMapper,
                projectContextParameterMapper);
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }

}
