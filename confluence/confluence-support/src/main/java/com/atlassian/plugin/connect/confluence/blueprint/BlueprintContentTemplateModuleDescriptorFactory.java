package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.confluence.languages.DefaultLocaleManager;
import com.atlassian.confluence.plugins.createcontent.extensions.ContentTemplateModuleDescriptor;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.lifecycle.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.api.util.Dom4jUtils;
import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateContextBean;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.sal.api.net.RequestFactory;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@link BlueprintModuleBean} to {@link WebItemModuleDescriptor} part of the blueprint mapping.
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
    public ContentTemplateModuleDescriptor createModuleDescriptor(BlueprintModuleBean bean, ConnectAddonBean addon, Plugin plugin)
    {
        Element contentTemplateElement = new DOMElement("content-template");
        String contentTemplateKey = BlueprintUtils.getContentTemplateKey(addon, bean);

        String i18nKeyOrName = bean.getName().hasI18n() ? bean.getName().getI18n() : bean.getDisplayName();
        contentTemplateElement.addAttribute("key", contentTemplateKey);
        contentTemplateElement.addAttribute("i18n-name-key", i18nKeyOrName);

        contentTemplateElement.addElement("resource")
                .addAttribute("name", "template")
                .addAttribute("type", "download")
                .addAttribute("location", createTemplateURL(addon.getBaseUrl(), bean.getBlueprintTemplate().getUrl()));

        BlueprintTemplateContextBean blueprintContext = bean.getBlueprintTemplate().getBlueprintContext();

        if (blueprintContext != null)
        {
            String contextUrl = blueprintContext.getUrl();
            if (StringUtils.isBlank(contextUrl))
            {
                throw new RuntimeException("The connect addon module '" + bean.getKey(addon)  + "' has a blueprint template context url field, but it is blank.");
            }
            else
            {
                Element contextProvider = contentTemplateElement.addElement("context-provider");
                contextProvider.addAttribute("class", BlueprintContextProvider.class.getName());
                contextProvider.addElement("param")
                               .addAttribute("name", BlueprintContextProvider.CONTEXT_URL_KEY)
                               .addAttribute("value", contextUrl);
                contextProvider.addElement("param")
                               .addAttribute("name", BlueprintContextProvider.REMOTE_ADDON_KEY)
                               .addAttribute("value", addon.getKey());
                contextProvider.addElement("param")
                               .addAttribute("name", BlueprintContextProvider.CONTENT_TEMPLATE_KEY)
                               //we want the raw key since this is going to be sent to the connect plugin, and they can't decode our full key format
                               .addAttribute("value", bean.getRawKey());
            }
        }

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
