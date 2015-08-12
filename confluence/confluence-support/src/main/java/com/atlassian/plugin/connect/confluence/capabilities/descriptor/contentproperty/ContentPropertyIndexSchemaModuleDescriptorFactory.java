package com.atlassian.plugin.connect.confluence.capabilities.descriptor.contentproperty;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.UISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.api.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringWriter;

@ConfluenceComponent
public class ContentPropertyIndexSchemaModuleDescriptorFactory implements
        ConnectModuleDescriptorFactory<ContentPropertyModuleBean, ContentPropertyIndexSchemaModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(ContentPropertyIndexSchemaModuleDescriptorFactory.class);

    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public ContentPropertyIndexSchemaModuleDescriptorFactory(ConnectContainerUtil connectContainerUtil)
    {
        this.connectContainerUtil = connectContainerUtil;
    }

    @Override
    public ContentPropertyIndexSchemaModuleDescriptor createModuleDescriptor(
            ConnectModuleProviderContext moduleProviderContext, Plugin plugin,
            ContentPropertyModuleBean bean)
    {
        ContentPropertyIndexSchemaModuleDescriptor descriptor = connectContainerUtil.createBean
                (ContentPropertyIndexSchemaModuleDescriptor.class);
        descriptor.init(plugin, createXmlConfig(moduleProviderContext, bean));
        return descriptor;
    }

    private Element createXmlConfig(ConnectModuleProviderContext moduleProviderContext,
            ContentPropertyModuleBean bean)
    {
        ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        Element indexSchema = new DOMElement("content-property-index-schema");
        indexSchema.addAttribute("key", bean.getKey(connectAddonBean));
        String addonBaseUrl = connectAddonBean.getBaseUrl();

        for (ContentPropertyIndexKeyConfigurationBean keyConfigurationBean : bean.getKeyConfigurations())
        {
            Element keyConfigurationElement = indexSchema.addElement("key");
            keyConfigurationElement.addAttribute("property-key", keyConfigurationBean.getPropertyKey());

            for (ContentPropertyIndexExtractionConfigurationBean extractionBean : keyConfigurationBean.getExtractions())
            {
                Element propertyExtractionElement = keyConfigurationElement.addElement("extract");
                propertyExtractionElement.addAttribute("path", extractionBean.getObjectName());
                propertyExtractionElement.addAttribute("type", extractionBean.getType().toString());
                propertyExtractionElement.addAttribute("alias", extractionBean.getAlias());

                UISupportModuleBean uiSupportBean = extractionBean.getUiSupport();
                if (uiSupportBean != null) {
                    Element uiSupportElement = propertyExtractionElement.addElement("ui-support");

                    uiSupportElement.addAttribute("value-type", extractionBean.getType().toString());
                    if (uiSupportBean.getDefaultOperator() == null)
                    {
                        uiSupportElement.addAttribute("default-operator", extractionBean.getType() == ContentPropertyIndexFieldType.text ? "~" : "=");
                    }
                    else
                    {
                        uiSupportElement.addAttribute("default-operator", uiSupportBean.getDefaultOperator());
                    }
                    if (uiSupportBean.getName() != null)
                    {
                        uiSupportElement.addAttribute("i18n-key", uiSupportBean.getName().getKeyOrValue());
                    }
                    if (uiSupportBean.getTooltip() != null)
                    {
                        uiSupportElement.addAttribute("i18n-field-tooltip", uiSupportBean.getTooltip().getKeyOrValue());
                    }
                    String relativeDataUri = uiSupportBean.getDataUri();
                    if (relativeDataUri != null)
                    {
                        uiSupportElement.addAttribute("data-uri", addonBaseUrl + uiSupportBean.getDataUri());
                    }

                }

            }
        }

        logSchema(indexSchema);
        return indexSchema;
    }

    private void logSchema(Element indexSchema)
    {
        if (log.isDebugEnabled())
        {
            try
            {
                StringWriter writer = new StringWriter();
                indexSchema.write(writer);
                log.debug(writer.toString());
            }
            catch (IOException ex)
            {
                log.warn("Error attempting to log schema ", ex);
            }
        }
    }
}
