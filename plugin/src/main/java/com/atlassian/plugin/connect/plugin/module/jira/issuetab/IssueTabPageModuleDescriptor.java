package com.atlassian.plugin.connect.plugin.module.jira.issuetab;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractJiraTabPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.web.Condition;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remote issue tab that loads is contents from an iframe
 */
public final class IssueTabPageModuleDescriptor extends AbstractJiraTabPageModuleDescriptor
{
    private static final String ISSUE_TAB_PAGE_MODULE_PREFIX = "issue-tab-page-";

    private final IFrameRendererImpl iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public IssueTabPageModuleDescriptor(final ModuleFactory moduleFactory, final DynamicDescriptorRegistration dynamicDescriptorRegistration, final ConditionProcessor conditionProcessor, final IFrameRendererImpl iFrameRenderer, final UrlVariableSubstitutor urlVariableSubstitutor, final JiraAuthenticationContext jiraAuthenticationContext, final UrlValidator urlValidator)
    {
        super(moduleFactory, dynamicDescriptorRegistration, conditionProcessor, urlValidator);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
    }

    @Override
    public String getModulePrefix()
    {
        return ISSUE_TAB_PAGE_MODULE_PREFIX;
    }

    protected JiraResourcedModuleDescriptor createTabPanelModuleDescriptor(final String key, final IFrameParams iFrameParams, final Condition condition)
    {
        return new IssueTabPanelModuleDescriptorImpl(
                jiraAuthenticationContext, new ModuleFactory()
        {
            @Override
            public <T> T createModule(final String name, final ModuleDescriptor<T> moduleDescriptor)
                    throws PluginParseException
            {
                return (T) new IFrameIssueTab(
                        new IFrameContextImpl(getPluginKey() , url, key, iFrameParams),
                        iFrameRenderer, Optional.fromNullable(condition), urlVariableSubstitutor);
            }
        });

    }
}
