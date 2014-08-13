package com.atlassian.plugin.connect.plugin.capabilities.descriptor.report;


import com.atlassian.jira.plugin.report.Report;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.plugin.report.ReportModuleDescriptorImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.jira.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.connect.plugin.integration.plugins.DynamicDescriptorRegistration;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

/**
 * Connect version of JIRA report module.
 *
 * @since 1.2
 */
public class ConnectReportModuleDescriptor extends AbstractModuleDescriptor<Void>
{
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    private final JiraAuthenticationContext authContext;
    private final DynamicDescriptorRegistration dynamicDescriptorRegistration;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final ModuleContextFilter moduleContextFilter;

    private DynamicDescriptorRegistration.Registration registration;
    private Element descriptor;

    public ConnectReportModuleDescriptor(JiraAuthenticationContext authenticationContext,
            ModuleFactory moduleFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            DynamicDescriptorRegistration dynamicDescriptorRegistration,
            UrlVariableSubstitutor urlVariableSubstitutor,
            ModuleContextFilter moduleContextFilter)
    {
        super(moduleFactory);
        this.authContext = authenticationContext;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.dynamicDescriptorRegistration = dynamicDescriptorRegistration;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.moduleContextFilter = moduleContextFilter;
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.descriptor = element;
    }

    @Override
    public void enabled()
    {
        super.enabled();
        this.registration = dynamicDescriptorRegistration.registerDescriptors(plugin, getReportDescriptor());
    }

    @Override
    public void disabled()
    {
        if (registration != null)
        {
            registration.unregister();
        }
        super.disabled();
    }

    private DescriptorToRegister getReportDescriptor()
    {
        ReportModuleDescriptor moduleDescriptor = new ReportModuleDescriptorImpl(authContext, moduleFactory)
        {
            @Override
            public Report getModule()
            {
                return new ConnectReport(iFrameRenderStrategyRegistry, getPluginKey(), getKey());
            }

            @Override
            public String getUrl(final Project project)
            {
                final String url = descriptor.attribute("url").getValue();
                final JiraModuleContextParameters unfilteredContext = new JiraModuleContextParametersImpl();
                unfilteredContext.addProject(project);

                return urlVariableSubstitutor.append(url, moduleContextFilter.filter(unfilteredContext));
            }
        };
        moduleDescriptor.init(plugin, descriptor);
        return new DescriptorToRegister(moduleDescriptor);
    }

    @Override
    public Void getModule()
    {
        return null;
    }
}
