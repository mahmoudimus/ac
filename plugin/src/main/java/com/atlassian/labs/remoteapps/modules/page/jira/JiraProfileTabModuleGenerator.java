package com.atlassian.labs.remoteapps.modules.page.jira;

import com.atlassian.labs.jira4compat.CompatViewProfilePanelModuleDescriptor;
import com.atlassian.labs.jira4compat.spi.CompatViewProfilePanelFactory;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyDescriptorXml;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;

/**
 *
 */
public class JiraProfileTabModuleGenerator implements RemoteModuleGenerator
{
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final CompatViewProfilePanelFactory compatViewProfilePanelFactory;
    private Map<String, Object> iframeParams = newHashMap();

    public JiraProfileTabModuleGenerator(TemplateRenderer templateRenderer,
                                         WebResourceManager webResourceManager,
                                         CompatViewProfilePanelFactory compatViewProfilePanelFactory,
                                         ApplicationLinkOperationsFactory applicationLinkOperationsFactory)
    {
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.compatViewProfilePanelFactory = compatViewProfilePanelFactory;
    }

    @Override
    public String getType()
    {
        return "profile-page";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return Collections.singleton("compat-view-profile-panel");
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
        addToParams(e, "height");
        addToParams(e, "width");

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
    public void validate(Element element) throws PluginParseException
    {
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    private void addToParams(Element e, String key)
    {
        String val = e.attributeValue(key);
        if (val != null)
        {
            iframeParams.put(key, val);
        }
    }

    private CompatViewProfilePanelModuleDescriptor createProfilePanelDescriptor(final RemoteAppCreationContext ctx,
                                                            Element e,
                                                            String key,
                                                            final String path
    )
    {
        final String panelName = getRequiredAttribute(e, "name");
        Element config = copyDescriptorXml(e);
        config.addAttribute("key", "profile-" + key);
        config.addAttribute("i18n-key", panelName);
        config.addAttribute("class", IFrameViewProfilePanel.class.getName());

        final CompatViewProfilePanelModuleDescriptor descriptor = new CompatViewProfilePanelModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                ApplicationLinkOperationsFactory.LinkOperations linkOps = applicationLinkOperationsFactory.create(ctx.getApplicationType());
                return (T) new IFrameViewProfilePanel(templateRenderer, webResourceManager, linkOps, iframeParams, panelName, path);
            }
        }, ctx.getBundle().getBundleContext(), compatViewProfilePanelFactory);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }
}
