package com.atlassian.labs.remoteapps.modules.panel.jira;

import com.atlassian.labs.jira4compat.CompatViewProfilePanelModuleDescriptor;
import com.atlassian.labs.jira4compat.spi.CompatViewProfilePanelFactory;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptorFactory;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebPanel;

import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import static java.util.Collections.emptySet;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class ViewIssuePanelModuleGenerator implements RemoteModuleGenerator
{
    private final IFrameRenderer iFrameRenderer;
    private Map<String, Object> iframeParams = newHashMap();
    private final WebInterfaceManager webInterfaceManager;
    private final ModuleFactory moduleFactory;
    private final HostContainer hostContainer;

    public ViewIssuePanelModuleGenerator(final IFrameRenderer iFrameRenderer, final WebInterfaceManager webInterfaceManager, final ModuleFactory moduleFactory, final HostContainer hostContainer)
    {
        this.iFrameRenderer = iFrameRenderer;
        this.webInterfaceManager = webInterfaceManager;
        this.moduleFactory = moduleFactory;
        this.hostContainer = hostContainer;
    }

    @Override
    public String getType()
    {
        return "issue-panel";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return emptySet();
    }


    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element element)
    {
        Element desc = element.createCopy();
        ModuleDescriptor<WebPanel> moduleDescriptor = null;

        desc.addAttribute("location", "atl.jira.view.issue.right.context");
        Element resourceChild = desc.addElement("resource");
        resourceChild.addAttribute("name", "view");
        resourceChild.addAttribute("type", "static");
        resourceChild.addCDATA("<b>Hello World 2!</b>");
        try
        {
            // Explicit instantiation feels wrong here - should I be using a ModuleDescriptor factory? which one? Where do I get it?
            moduleDescriptor = new DefaultWebPanelModuleDescriptor(hostContainer, moduleFactory, webInterfaceManager);
            moduleDescriptor.init(ctx.getPlugin(), desc);
        }
        catch (Exception ex)
        {
            throw new PluginParseException(ex);
        }

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

    @Override
    public void validate(Element element, String registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

}
