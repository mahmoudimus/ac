package com.atlassian.plugin.connect.plugin.module.jira.versiontab;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.integration.plugins.LegacyXmlDynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.ConditionProcessor;
import com.atlassian.plugin.connect.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.connect.plugin.module.jira.AbstractJiraTabPageModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.ProjectSerializer;
import com.atlassian.plugin.connect.plugin.module.jira.context.serializer.VersionSerializer;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlValidator;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.Condition;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A remote version tab that loads its contents from an iframe
 */
public class VersionTabPageModuleDescriptor extends AbstractJiraTabPageModuleDescriptor
{
    public static final String VERSION_TAB_PAGE_MODULE_PREFIX = "version-tab-";

    private final IFrameRendererImpl iFrameRenderer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final VersionSerializer versionSerializer;
    private final ProjectSerializer projectSerializer;

    public VersionTabPageModuleDescriptor(ModuleFactory moduleFactory, LegacyXmlDynamicDescriptorRegistration dynamicDescriptorRegistration, ConditionProcessor conditionProcessor,
            IFrameRendererImpl iFrameRenderer, UrlVariableSubstitutor urlVariableSubstitutor, JiraAuthenticationContext jiraAuthenticationContext,
            UrlValidator urlValidator, VersionSerializer versionSerializer, ProjectSerializer projectSerializer)
    {
        super(moduleFactory, dynamicDescriptorRegistration, conditionProcessor, urlValidator);
        this.versionSerializer = checkNotNull(versionSerializer);
        this.projectSerializer = checkNotNull(projectSerializer);
        this.iFrameRenderer = checkNotNull(iFrameRenderer);
        this.urlVariableSubstitutor = checkNotNull(urlVariableSubstitutor);
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
    }

    @Override
    public String getModulePrefix()
    {
        return VERSION_TAB_PAGE_MODULE_PREFIX;
    }

    protected JiraResourcedModuleDescriptor createTabPanelModuleDescriptor(final String key, final IFrameParams iFrameParams, final Condition condition)
    {
        return new VersionTabPanelModuleDescriptorImpl(
                jiraAuthenticationContext, new ModuleFactory()
        {
            @Override
            public <T> T createModule(final String name, final ModuleDescriptor<T> moduleDescriptor)
                    throws PluginParseException
            {
                return (T) new IFrameVersionTab(
                        new IFrameContextImpl(getPluginKey(), url, key, iFrameParams),
                        iFrameRenderer, condition, urlVariableSubstitutor, projectSerializer, versionSerializer);
            }
        });
    }

    @Override
    protected Class getIFrameTabClass()
    {
        return IFrameVersionTab.class;
    }

    @Override
    public String getModuleClassName()
    {
        return super.getModuleClassName();
    }
}
