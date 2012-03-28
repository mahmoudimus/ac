package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.*;
import com.atlassian.labs.remoteapps.modules.external.*;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;

/**
 * Abstract module type for canvas pages, generating a web item and servlet with iframe
 */
public abstract class AbstractPageModuleGenerator implements RemoteModuleGenerator
{
    private final ProductAccessor productAccessor;
    private final ServletModuleManager servletModuleManager;
    private final TemplateRenderer templateRenderer;
    private final UserManager userManager;
    private final ApplicationLinkOperationsFactory applicationLinkSignerFactory;
    private final WebItemCreator webItemCreator;
    private final IFrameRenderer iFrameRenderer;
    private final Plugin plugin;

    @Autowired
    public AbstractPageModuleGenerator(ServletModuleManager servletModuleManager,
            TemplateRenderer templateRenderer,
            ApplicationLinkOperationsFactory applicationLinkSignerFactory,
            IFrameRenderer iFrameRenderer,
            WebItemContext webItemContext,
            UserManager userManager, ProductAccessor productAccessor,
            PluginRetrievalService pluginRetrievalService)
    {
        this.servletModuleManager = servletModuleManager;
        this.templateRenderer = templateRenderer;
        this.applicationLinkSignerFactory = applicationLinkSignerFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
        this.productAccessor = productAccessor;
        this.webItemCreator = new WebItemCreator(webItemContext, this.productAccessor);
        this.plugin = pluginRetrievalService.getPlugin();
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(getPlugin(),
                "page.xsd",
                "/xsd/page.xsd",
                "PageType",
                "unbounded");
    }

    @Override
    public void validate(Element element, String registrationUrl, String username) throws PluginParseException
    {
    }

    @Override
    public RemoteModule generate(RemoteAppCreationContext ctx, Element e)
    {
        String key = getRequiredAttribute(e, "key");
        final String url = getRequiredAttribute(e, "url");

        String localUrl = "/remoteapps/" + ctx.getApplicationType().getId().get() + "/" + key;

        final Set<ModuleDescriptor> descriptors = ImmutableSet.<ModuleDescriptor>of(
                createServletDescriptor(ctx, e, key, url, localUrl),
                webItemCreator.createWebItemDescriptor(ctx, e, key, localUrl, getCondition(), getWebItemStyleClass()));
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
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
        final String moduleKey = "servlet-" + key;
        config.addAttribute("key", moduleKey);
        config.addAttribute("class", IFramePageServlet.class.getName());
        config.addElement("url-pattern").setText(localUrl + "");
        config.addElement("url-pattern").setText(localUrl + "/*");

        final IFrameParams params = new IFrameParams(e);
        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                PageInfo pageInfo = new PageInfo(getDecorator(), getTemplateSuffix(), pageName, getCondition());
                
                return (T) new IFramePageServlet(
                        pageInfo,
                        templateRenderer, iFrameRenderer,
                        new IFrameContext(applicationLinkSignerFactory.create(ctx.getApplicationType()), path, moduleKey, params), userManager
                        );
            }
        }, servletModuleManager);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }

    protected Condition getCondition()
    {
        return new AlwaysDisplayCondition();
    }

    protected String getTemplateSuffix()
    {
        return "";
    }

    protected String getWebItemStyleClass()
    {
        return "";
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    protected Plugin getPlugin()
    {
        return plugin;
    }

    protected abstract String getDecorator();
}
