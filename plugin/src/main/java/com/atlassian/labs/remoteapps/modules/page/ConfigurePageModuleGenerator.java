package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.external.Schema;
import com.atlassian.labs.remoteapps.modules.external.StaticSchema;
import com.atlassian.labs.remoteapps.product.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Module type for configuration pages in the admin area, generating a web item and servlet with
 * iframe but also add a configuration URL to the plugin descriptor
 */
@Component
public class ConfigurePageModuleGenerator extends AdminPageModuleGenerator
{
    @Autowired
    public ConfigurePageModuleGenerator(ServletModuleManager servletModuleManager,
            TemplateRenderer templateRenderer, ProductAccessor productAccessor,
            ApplicationLinkOperationsFactory applicationLinkSignerFactory,
            IFrameRenderer iFrameRenderer, PluginRetrievalService pluginRetrievalService,
            UserManager userManager)
    {
        super(servletModuleManager, templateRenderer, productAccessor, applicationLinkSignerFactory,
                iFrameRenderer, pluginRetrievalService, userManager);
    }

    @Override
    public String getType()
    {
        return "configure-page";
    }

    @Override
    public String getName()
    {
        return "Configure Page";
    }

    @Override
    public Schema getSchema()
    {
        return new StaticSchema(getPlugin(),
                "page.xsd",
                "/xsd/page.xsd",
                "PageType",
                "1");
    }

    @Override
    public String getDescription()
    {
        return "The configuration page for the app, decorated in the admin section, with a link in" +
                "the admin menu and a configure link in the Plugin Manager";
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        String appKey = pluginDescriptorRoot.attributeValue("key");
        String pageKey = descriptorElement.attributeValue("key");
        pluginDescriptorRoot.element("plugin-info").addElement("param")
                .addAttribute("name", "configure.url")
                .addText("/plugins/servlet" + createLocalUrl(appKey, pageKey));
    }
}
