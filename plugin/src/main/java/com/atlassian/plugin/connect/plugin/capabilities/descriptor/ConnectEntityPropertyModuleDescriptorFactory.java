package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptor;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ConnectEntityPropertyModuleDescriptorFactory implements ConnectModuleDescriptorFactory<EntityPropertyModuleBean, EntityPropertyIndexDocumentModuleDescriptor>
{
    public static final String DESCRIPTOR_NAME = "index-document-configuration";

    private final ConnectContainerUtil autowireUtil;

    @Autowired
    public ConnectEntityPropertyModuleDescriptorFactory(ConnectContainerUtil autowireUtil)
    {
        this.autowireUtil = autowireUtil;
    }

    @Override
    public EntityPropertyIndexDocumentModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, EntityPropertyModuleBean bean)
    {
        Element indexDocumentConfiguration = new DOMElement(DESCRIPTOR_NAME);

        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        indexDocumentConfiguration.addAttribute("key", bean.getKey(connectAddonBean));
        indexDocumentConfiguration.addAttribute("entity-key", bean.getEntityType().getValue());
        indexDocumentConfiguration.addAttribute("i18n-name-key", bean.getName().getI18n());

        for (EntityPropertyIndexKeyConfigurationBean keyConfigurationBean : bean.getKeyConfigurations())
        {
            final Element keyConfigurationElement = indexDocumentConfiguration.addElement("key");
            keyConfigurationElement.addAttribute("property-key", keyConfigurationBean.getPropertyKey());

            for (EntityPropertyIndexExtractionConfigurationBean extractionBean : keyConfigurationBean.getExtractions())
            {
                final Element propertyExtractionElement = keyConfigurationElement.addElement("extract");
                propertyExtractionElement.addAttribute("path", extractionBean.getObjectName());
                propertyExtractionElement.addAttribute("type", extractionBean.getType().toString());
            }
        }

        EntityPropertyIndexDocumentModuleDescriptorImpl descriptor = autowireUtil.createBean(EntityPropertyIndexDocumentModuleDescriptorImpl.class);
        descriptor.init(theConnectPlugin, indexDocumentConfiguration);

        return descriptor;
    }

}
