package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.languages.DefaultLocaleManager;
import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateModuleDescriptor;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.util.Dom4jUtils;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.net.RequestFactory;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@link com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean} to
 * {@link com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor} part of the blueprint
 * mapping.
 *
 * @see BlueprintModuleDescriptorFactory
 * @see BlueprintWebItemModuleDescriptorFactory
 */
@ConfluenceComponent
public class BlueprintContentTemplateModuleDescriptorFactory
        implements ConnectModuleDescriptorFactory<BlueprintModuleBean, ContentTemplateModuleDescriptor>
{

    private static final Logger log = LoggerFactory.getLogger(BlueprintContentTemplateModuleDescriptorFactory.class);

    private final ModuleFactory moduleFactory;
    private final I18NBeanFactory i18nBeanFactory;
    private final RequestFactory<?> requestFactory;

    @Autowired
    public BlueprintContentTemplateModuleDescriptorFactory(ModuleFactory moduleFactory,
                                                           I18NBeanFactory i18nBeanFactory,
                                                           RequestFactory<?> requestFactory)
    {
        this.moduleFactory = moduleFactory;
        this.i18nBeanFactory = i18nBeanFactory;
        this.requestFactory = requestFactory;
    }


    @Override
    public ContentTemplateModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin plugin, BlueprintModuleBean bean)
    {
        ConnectAddonBean addon = moduleProviderContext.getConnectAddonBean();

        Element contentTemplateElement = new DOMElement("content-template");
        String contentTemplateKey = BlueprintUtils.getContentTemplateKey(addon, bean);

        String i18nKeyOrName = !bean.getName().hasI18n() ? bean.getDisplayName() : bean.getName().getI18n();
        contentTemplateElement.addAttribute("key", contentTemplateKey);
        contentTemplateElement.addAttribute("i18n-name-key", i18nKeyOrName);

        contentTemplateElement.addElement("resource")
                .addAttribute("name", "template")
                .addAttribute("type", "download")
                .addAttribute("location", createTemplateURL(addon.getBaseUrl(), bean.getBlueprintTemplate().getUrl()));

        if (log.isDebugEnabled())
        {
            log.debug(Dom4jUtils.printNode(contentTemplateElement));
        }

        final ContentTemplateModuleDescriptor descriptor = new ContentTemplateModuleDescriptor(moduleFactory,
                i18nBeanFactory,
                new DefaultLocaleManager(),
                requestFactory);
        descriptor.init(plugin, contentTemplateElement);
        return descriptor;
    }

    public static void main(String[] args)
    {
        System.out.println(createTemplateURL("base", "resource"));
    }

    private static String createTemplateURL(String baseUrl, String blueprintResource)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(baseUrl);
        if (!baseUrl.endsWith("/") && !blueprintResource.startsWith("/"))
        {
            buffer.append("/").append(blueprintResource);
        }
        else if (baseUrl.endsWith("/") && blueprintResource.startsWith("/"))
        {
            buffer.append(blueprintResource.substring(1));
        }
        else
        {
            buffer.append(blueprintResource);
        }
        return buffer.toString();
    }

}
