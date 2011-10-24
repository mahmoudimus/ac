package com.atlassian.labs.remoteapps.modules.admin;

import com.atlassian.labs.remoteapps.modules.IFramePageServlet;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.util.Collections;
import java.util.Set;

/**
 *
 */
public class AdminPageModuleGenerator implements RemoteModuleGenerator
{
    private final ServletModuleManager servletModuleManager;
    private final TemplateRenderer templateRenderer;
    private final ProductAccessor productAccessor;
    private final WebResourceManager webResourceManager;

    public AdminPageModuleGenerator(ServletModuleManager servletModuleManager, TemplateRenderer templateRenderer, ProductAccessor productAccessor, WebResourceManager webResourceManager)
    {
        this.servletModuleManager = servletModuleManager;
        this.templateRenderer = templateRenderer;
        this.productAccessor = productAccessor;
        this.webResourceManager = webResourceManager;
    }

    @Override
    public String getType()
    {
        return "admin-page";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return Collections.emptySet();
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element e)
    {
        String key = e.attributeValue("key");
        final String url = e.attributeValue("url");

        final String fullUrl = e.getParent().attributeValue("display-url") + url;
        String localUrl = "/" + ctx.getApplicationType().getId().get() + "/" + key;

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(
                createServletDescriptor(ctx, e, key, fullUrl, localUrl),
                createWebItemDescriptor(ctx, e, key, fullUrl, localUrl)
        );
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    private ServletModuleDescriptor createServletDescriptor(RemoteAppCreationContext ctx, Element e, String key, final String fullUrl, String localUrl)
    {
        final String pageName = e.attributeValue("name");
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
                return (T) new IFramePageServlet(templateRenderer, pageName, fullUrl, "atl.admin", webResourceManager);
            }
        }, servletModuleManager);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }

    private WebItemModuleDescriptor createWebItemDescriptor(RemoteAppCreationContext ctx, Element e, String key, final String fullUrl, String localUrl)
    {
        Element config = e.createCopy();
        config.addAttribute("key", "webitem-" + key);
        config.addAttribute("section", productAccessor.getPreferredAdminSectionKey());
        config.addAttribute("weight", String.valueOf(productAccessor.getPreferredAdminWeight()));

        String name = e.attributeValue("name");
        config.addElement("label").setText(name);
        config.addElement("link").setText("/plugins/servlet" + localUrl);

        WebItemModuleDescriptor descriptor = productAccessor.createWebItemModuleDescriptor();
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }
}
