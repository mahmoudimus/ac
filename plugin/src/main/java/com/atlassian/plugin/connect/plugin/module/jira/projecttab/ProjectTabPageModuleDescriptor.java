package com.atlassian.plugin.connect.plugin.module.jira.projecttab;

import com.atlassian.jira.plugin.TabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractJiraTabPageModuleDescriptor;
import com.atlassian.plugin.web.Condition;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remote project tab that loads its contents from an iframe
 */
public final class ProjectTabPageModuleDescriptor extends AbstractJiraTabPageModuleDescriptor
{
    public static final String PROJECT_TAB_PAGE_MODULE_PREFIX = "project-tab-";

    private final IFrameRendererImpl iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectSerializer projectSerializer;

    public ProjectTabPageModuleDescriptor(ModuleFactory moduleFactory, LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration, ConditionProcessor conditionProcessor,
            IFrameRendererImpl iFrameRenderer, JiraAuthenticationContext jiraAuthenticationContext, UrlValidator urlValidator, UrlVariableSubstitutor urlVariableSubstitutor,
            ProjectSerializer projectSerializer)
    {
        super(moduleFactory, dynamicDescriptorRegistration, conditionProcessor, urlValidator);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.projectSerializer = checkNotNull(projectSerializer);
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
                        iFrameRenderer, condition, urlVariableSubstitutor, projectSerializer);
            }
        });
    }

    @Override
    protected Class getIFrameTabClass()
    {
        return IFrameProjectTab.class;
    }
}
