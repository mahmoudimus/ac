package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyIndexSchemaModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ContentPropertyIndexSchemaModuleDescriptorFactory implements
        ConnectModuleDescriptorFactory<ContentPropertyIndexSchemaModuleBean, ContentPropertyIndexSchemaModuleDescriptor>
{
    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public ContentPropertyIndexSchemaModuleDescriptorFactory(ConnectContainerUtil connectContainerUtil)
    {
        this.connectContainerUtil = connectContainerUtil;
    }

    @Override
    public ContentPropertyIndexSchemaModuleDescriptor createModuleDescriptor(
            ConnectModuleProviderContext moduleProviderContext, Plugin plugin,
            ContentPropertyIndexSchemaModuleBean bean)
    {
        ContentPropertyIndexSchemaModuleDescriptor descriptor = connectContainerUtil.createBean
                (ContentPropertyIndexSchemaModuleDescriptor.class);
        descriptor.init(plugin, createXmlConfig(moduleProviderContext, bean));
        return descriptor;
    }

    private Element createXmlConfig(ConnectModuleProviderContext moduleProviderContext,
            ContentPropertyIndexSchemaModuleBean bean)
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
                propertyExtractionElement.addAttribute("path", extractionBean.getPath());
                propertyExtractionElement.addAttribute("type", extractionBean.getType());
            }
        }

        return indexSchema;
    }
}
