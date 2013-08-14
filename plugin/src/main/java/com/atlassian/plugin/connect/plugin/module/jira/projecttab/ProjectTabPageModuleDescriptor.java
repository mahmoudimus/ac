package com.atlassian.plugin.connect.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractJiraTabPageModuleDescriptor;
import com.atlassian.plugin.web.Condition;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remote project tab that loads is contents from an iframe
 */
public final class ProjectTabPageModuleDescriptor extends AbstractJiraTabPageModuleDescriptor
{
    public static final String PROJECT_TAB_PAGE_MODULE_PREFIX = "project-tab-";

    private final IFrameRendererImpl iFrameRenderer;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectTabPageModuleDescriptor(final ModuleFactory moduleFactory, final DynamicDescriptorRegistration dynamicDescriptorRegistration, final ConditionProcessor conditionProcessor, final IFrameRendererImpl iFrameRenderer, final JiraAuthenticationContext jiraAuthenticationContext, final UrlValidator urlValidator)
    {
        super(moduleFactory, dynamicDescriptorRegistration, conditionProcessor, urlValidator);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
    }

    @Override
    public String getModulePrefix()
    {
        return PROJECT_TAB_PAGE_MODULE_PREFIX;
    }

    protected TabPanelModuleDescriptor createTabPanelModuleDescriptor(final String key, final IFrameParams iFrameParams, final Condition condition)
    {
        return new ProjectTabPanelModuleDescriptorImpl(
                jiraAuthenticationContext, new ModuleFactory()
        {
            @Override
            public <T> T createModule(final String name, final ModuleDescriptor<T> moduleDescriptor)
                    throws PluginParseException
            {
                return (T) new IFrameProjectTab(
                        new IFrameContextImpl(getPluginKey(), url, key, iFrameParams),
                        iFrameRenderer, condition);
            }
        });
    }

    @Override
    protected Class getIFrameTabClass()
    {
        return IFrameProjectTab.class;
    }
}
