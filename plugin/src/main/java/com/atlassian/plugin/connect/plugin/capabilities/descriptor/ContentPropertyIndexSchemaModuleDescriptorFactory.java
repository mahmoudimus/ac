package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
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

        for (ContentPropertyIndexKeyConfigurationBean keyConfigurationBean : bean.getKeyConfigurations())
        {
            Element keyConfigurationElement = indexSchema.addElement("key");
            keyConfigurationElement.addAttribute("property-key", keyConfigurationBean.getPropertyKey());

            for (ContentPropertyIndexExtractionConfigurationBean extractionBean : keyConfigurationBean.getExtractions())
            {
                Element propertyExtractionElement = keyConfigurationElement.addElement("extract");
                propertyExtractionElement.addAttribute("path", extractionBean.getObjectName());
                propertyExtractionElement.addAttribute("type", extractionBean.getType().toString());
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
