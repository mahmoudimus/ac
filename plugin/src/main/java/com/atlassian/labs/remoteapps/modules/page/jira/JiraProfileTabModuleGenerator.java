package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.labs.jira4compat.CompatViewProfilePanelModuleDescriptor;
import com.atlassian.labs.jira4compat.spi.CompatViewProfilePanelFactory;
import com.atlassian.labs.remoteapps.loader.AggregateModuleDescriptorFactory;
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
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class JiraProfileTabModuleGenerator implements WaitableRemoteModuleGenerator
{
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final CompatViewProfilePanelFactory compatViewProfilePanelFactory;
    private final AggregateModuleDescriptorFactory moduleDescriptorFactory;
    private final IFrameRenderer iFrameRenderer;
    private final Plugin plugin;
    private Map<String, Object> iframeParams = newHashMap();

    public JiraProfileTabModuleGenerator(
            CompatViewProfilePanelFactory compatViewProfilePanelFactory,
            ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            IFrameRenderer iFrameRenderer,
            AggregateModuleDescriptorFactory moduleDescriptorFactory,
            PluginRetrievalService pluginRetrievalService)
    {
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.compatViewProfilePanelFactory = compatViewProfilePanelFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public String getType()
    {
        return "profile-page";
    }

    @Override
    public String getName()
    {
        return "User Profile Page";
    }

    @Override
    public String getDescription()
    {
        return "A user profile page decorated as a user profile tab";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(plugin,
                "page.xsd",
                "/xsd/page.xsd",
                "PageType");
    }

    @Override
    public void waitToLoad(Element element)
    {
        moduleDescriptorFactory.waitForRequiredDescriptors("compat-view-profile-panel");
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        return emptyMap();
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element e)
    {
        String key = getRequiredAttribute(e, "key");
        final String url = getRequiredAttribute(e, "url");

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(
                createProfilePanelDescriptor(ctx, e, key, url));
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
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    private CompatViewProfilePanelModuleDescriptor createProfilePanelDescriptor(final RemoteAppCreationContext ctx,
                                                            final Element e,
                                                            String key,
                                                            final String path
    )
    {
        final String panelName = getRequiredAttribute(e, "name");
        Element config = e.createCopy();
        final String moduleKey = "profile-" + key;
        config.addAttribute("key", moduleKey);
        config.addAttribute("i18n-key", panelName);
        config.addAttribute("class", IFrameViewProfilePanel.class.getName());

        final CompatViewProfilePanelModuleDescriptor descriptor = new CompatViewProfilePanelModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                ApplicationLinkOperationsFactory.LinkOperations linkOps = applicationLinkOperationsFactory.create(ctx.getApplicationType());
                return (T) new IFrameViewProfilePanel(
                        iFrameRenderer,
                        new IFrameContext(linkOps, path, moduleKey, new IFrameParams(e)));
            }
        }, ctx.getBundle().getBundleContext(), compatViewProfilePanelFactory);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }
}
