package com.atlassian.labs.remoteapps.modules.jira;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class ProjectTabModuleGenerator implements RemoteModuleGenerator
{
    private final IFrameRenderer iFrameRenderer;
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final Plugin plugin;

    public ProjectTabModuleGenerator(final IFrameRenderer iFrameRenderer,
            final ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            PluginRetrievalService pluginRetrievalService)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "project-tab";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
            "page.xsd",
            "/xsd/page.xsd",
            "PageType",
            "unbounded");
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, final Element element)
    {
        final String moduleKey = "project-tab-" + getRequiredAttribute(element, "key");
        final String url = getRequiredAttribute(element, "url");
        final String panelName = getRequiredAttribute(element, "name");

        Element desc = element.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addElement("label").setText(panelName);
        desc.addAttribute("class", IFrameProjectTab.class.getName());

        ProjectTabPanelModuleDescriptor moduleDescriptor = createDescriptor(ctx,
                desc, moduleKey, url,
                new IFrameParams(element));

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(moduleDescriptor);
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    private ProjectTabPanelModuleDescriptor createDescriptor(
            final RemoteAppCreationContext ctx,
            final Element desc,
            final String moduleKey,
            final String url,
            final IFrameParams iFrameParams)
    {
        try
        {
            JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getInstance().getJiraAuthenticationContext();
            ProjectTabPanelModuleDescriptor descriptor = new FixedProjectTabPanelModuleDescriptor(
                    jiraAuthenticationContext, new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {
                    ApplicationLinkOperationsFactory.LinkOperations linkOps = applicationLinkOperationsFactory.create(ctx.getApplicationType());

                    return (T) new IFrameProjectTab(
                            new IFrameContext(linkOps , url, moduleKey, iFrameParams),
                            iFrameRenderer);
                }
            });

            descriptor.init(ctx.getPlugin(), desc);
            return descriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }

    @Override
    public void validate(Element element, String registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public String getName()
    {
        return "Project Tab";
    }

    @Override
    public String getDescription()
    {
        return "A remote page decorated as its own JIRA project tab";
    }
}
