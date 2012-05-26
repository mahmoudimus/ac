package com.atlassian.labs.remoteapps.modules.jira.issuepanel;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.labs.remoteapps.modules.page.jira.IFrameViewProfilePanel;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredUriAttribute;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class ViewIssuePanelModuleGenerator implements RemoteModuleGenerator
{
    private final IFrameRenderer iFrameRenderer;
    private final WebInterfaceManager webInterfaceManager;
    private final HostContainer hostContainer;
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final Plugin plugin;

    public ViewIssuePanelModuleGenerator(final IFrameRenderer iFrameRenderer,
                                         final WebInterfaceManager webInterfaceManager,
                                         final ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
                                         final HostContainer hostContainer,
                                         PluginRetrievalService pluginRetrievalService)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.webInterfaceManager = webInterfaceManager;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.hostContainer = hostContainer;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "issue-panel-page";
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
        final String moduleKey = "issue-panel-page-" + getRequiredAttribute(element, "key");
        final String url = getRequiredUriAttribute(element, "url").toString();
        final String panelName = getRequiredAttribute(element, "name");

        Element desc = element.createCopy();
        desc.addAttribute("key", moduleKey);
        desc.addAttribute("i18n-key", panelName);
        desc.addAttribute("class", IFrameViewProfilePanel.class.getName());
        desc.addAttribute("location", "atl.jira.view.issue.right.context");
        desc.addAttribute("class", IFrameViewIssuePanel.class.getName());

        ModuleDescriptor<WebPanel> moduleDescriptor = createWebPanelModuleDescriptor(ctx, desc, moduleKey, url, new IFrameParams(element));

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

    private ModuleDescriptor<WebPanel> createWebPanelModuleDescriptor(final RemoteAppCreationContext ctx,
                                                                      final Element desc,
                                                                      final String moduleKey,
                                                                      final String url,
                                                                      final IFrameParams iFrameParams)
    {
        try
        {
            ModuleDescriptor<WebPanel> moduleDescriptor = new DefaultWebPanelModuleDescriptor(hostContainer, new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {
                    ApplicationLinkOperationsFactory.LinkOperations linkOps = applicationLinkOperationsFactory.create(ctx.getApplicationType());

                    return (T) new IFrameViewIssuePanel(
                            iFrameRenderer,
                            new IFrameContext(linkOps , url, moduleKey, iFrameParams));
                }
            }, webInterfaceManager);

            moduleDescriptor.init(ctx.getPlugin(), desc);
            return moduleDescriptor;
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }
    }

    @Override
    public void validate(Element element, URI registrationUrl, String username) throws PluginParseException
    {
        getRequiredUriAttribute(element, "url");
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
        return "Issue Tab Page";
    }

    @Override
    public String getDescription()
    {
        return "A remote page decorated as a web panel on the view issue page";
    }
}
