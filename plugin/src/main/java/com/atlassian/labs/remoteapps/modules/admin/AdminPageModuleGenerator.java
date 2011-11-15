package com.atlassian.labs.remoteapps.modules.admin;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.labs.remoteapps.OAuthLinkManager;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.descriptor.factory.AggregateDescriptorFactory;
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
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyDescriptorXml;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Module type for admin pages, generating a web item and servlet with iframe
 */
@Component
public class AdminPageModuleGenerator implements RemoteModuleGenerator
{
    private final ServletModuleManager servletModuleManager;
    private final TemplateRenderer templateRenderer;
    private final ProductAccessor productAccessor;
    private final AggregateDescriptorFactory descriptorFactory;
    private final WebResourceManager webResourceManager;
    private final ApplicationLinkService applicationLinkService;
    private final OAuthLinkManager oAuthLinkManager;
    private final PermissionManager permissionManager;
    private Map<String,Object> iframeParams = newHashMap();

    @Autowired
    public AdminPageModuleGenerator(ServletModuleManager servletModuleManager,
                                    TemplateRenderer templateRenderer,
                                    ProductAccessor productAccessor,
                                    WebResourceManager webResourceManager,
                                    ApplicationLinkService applicationLinkService, OAuthLinkManager oAuthLinkManager, PermissionManager permissionManager, AggregateDescriptorFactory descriptorFactory)
    {
        this.servletModuleManager = servletModuleManager;
        this.templateRenderer = templateRenderer;
        this.productAccessor = productAccessor;
        this.webResourceManager = webResourceManager;
        this.applicationLinkService = applicationLinkService;
        this.oAuthLinkManager = oAuthLinkManager;
        this.permissionManager = permissionManager;
        this.descriptorFactory = descriptorFactory;
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
        String key = getRequiredAttribute(e, "key");
        final String url = getRequiredAttribute(e, "url");
        addToParams(e, "height");
        addToParams(e, "width");

        final String fullUrl = e.getParent().attributeValue("display-url") + url;
        String localUrl = "/" + ctx.getApplicationType().getId().get() + "/" + key;

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(
                createServletDescriptor(ctx, e, key, fullUrl, localUrl),
                createWebItemDescriptor(ctx, e, key, fullUrl, localUrl));
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
                                                            final String fullUrl,
                                                            String localUrl)
    {
        final String pageName = getRequiredAttribute(e, "name");
        Element config = copyDescriptorXml(e);
        config.addAttribute("key", "servlet-" + key);
        config.addAttribute("class", IFramePageServlet.class.getName());
        config.addElement("url-pattern").setText(localUrl + "");
        config.addElement("url-pattern").setText(localUrl + "/*");

        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                return (T) new IFramePageServlet(templateRenderer,
                        oAuthLinkManager,
                        applicationLinkService,
                        permissionManager,
                        ctx.getApplicationType(),
                        pageName,
                        fullUrl,
                        "atl.admin",
                        webResourceManager, iframeParams);
            }
        }, servletModuleManager);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }

    private ModuleDescriptor createWebItemDescriptor(RemoteAppCreationContext ctx,
                                                            Element e,
                                                            String key,
                                                            final String fullUrl,
                                                            String localUrl)
    {
        Element config = copyDescriptorXml(e);
        final String webItemKey = "webitem-" + key;
        config.addAttribute("key", webItemKey);
        config.addAttribute("section", getOptionalAttribute(e, "section", productAccessor.getPreferredAdminSectionKey()));
        config.addAttribute("weight", getOptionalAttribute(e, "weight", productAccessor.getPreferredAdminWeight()));

        String name = getRequiredAttribute(e, "name");
        config.addElement("label").setText(name);
        config.addElement("link").
                addAttribute("linkId", webItemKey).
                setText("/plugins/servlet" + localUrl);

        ModuleDescriptor descriptor = descriptorFactory.createWebItemModuleDescriptor(ctx.getBundle().getBundleContext(), ctx.getAccessLevel());
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }
}
