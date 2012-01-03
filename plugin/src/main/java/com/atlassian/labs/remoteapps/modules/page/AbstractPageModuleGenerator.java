package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.WebItemContext;
import com.atlassian.labs.remoteapps.modules.WebItemCreator;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Abstract module type for canvas pages, generating a web item and servlet with iframe
 */
public abstract class AbstractPageModuleGenerator implements RemoteModuleGenerator
{
    private final ServletModuleManager servletModuleManager;
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final ApplicationLinkOperationsFactory applicationLinkSignerFactory;
    private final WebItemCreator webItemCreator;
    private Map<String, Object> iframeParams = newHashMap();

    @Autowired
    public AbstractPageModuleGenerator(ServletModuleManager servletModuleManager,
                                       TemplateRenderer templateRenderer,
                                       WebResourceManager webResourceManager,
                                       ApplicationLinkOperationsFactory applicationLinkSignerFactory,
                                       WebItemContext webItemContext)
    {
        this.servletModuleManager = servletModuleManager;
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.applicationLinkSignerFactory = applicationLinkSignerFactory;
        this.webItemCreator = new WebItemCreator(webItemContext);
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return Collections.emptySet();
    }

    @Override
    public void validate(Element element) throws PluginParseException
    {
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element e)
    {
        String key = getRequiredAttribute(e, "key");
        final String url = getRequiredAttribute(e, "url");
        addToParams(e, "height");
        addToParams(e, "width");

        String localUrl = "/remoteapps/" + ctx.getApplicationType().getId().get() + "/" + key;

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(
                createServletDescriptor(ctx, e, key, url, localUrl),
                webItemCreator.createWebItemDescriptor(ctx, e, key, localUrl));
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    private void addToParams(Element e, String key)
    {
        String val = e.attributeValue(key);
        if (val != null)
        {
            iframeParams.put(key, val);
        }
    }

    private ServletModuleDescriptor createServletDescriptor(final RemoteAppCreationContext ctx,
                                                            Element e,
                                                            String key,
                                                            final String path,
                                                            String localUrl
    )
    {
        final String pageName = getRequiredAttribute(e, "name");
        Element config = e.createCopy();
        config.addAttribute("key", "servlet-" + key);
        config.addAttribute("class", IFramePageServlet.class.getName());
        config.addElement("url-pattern").setText(localUrl + "");
        config.addElement("url-pattern").setText(localUrl + "/*");

        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                return (T) new IFramePageServlet(templateRenderer, applicationLinkSignerFactory.create(ctx.getApplicationType()),
                        pageName, path, getDecorator(), webResourceManager, iframeParams);
            }
        }, servletModuleManager);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    protected abstract String getDecorator();
}
