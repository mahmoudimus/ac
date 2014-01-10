package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptor;
import com.atlassian.jira.plugin.index.EntityPropertyIndexDocumentModuleDescriptorImpl;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.EntityPropertyIndexDocumentModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ConnectEntityPropertyIndexDocumentModuleDescriptorFactory implements ConnectModuleDescriptorFactory<EntityPropertyIndexDocumentModuleBean, EntityPropertyIndexDocumentModuleDescriptor>
{
    public static final String DESCRIPTOR_NAME = "index-document-configuration";

    private final ConnectAutowireUtil autowireUtil;

    @Autowired
    public ConnectEntityPropertyIndexDocumentModuleDescriptorFactory(ConnectAutowireUtil autowireUtil)
    {
        this.autowireUtil = autowireUtil;
    }

    @Override
    public EntityPropertyIndexDocumentModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext,
            EntityPropertyIndexDocumentModuleBean bean)
    {
        Element indexDocumentConfiguration = new DOMElement(DESCRIPTOR_NAME);

        indexDocumentConfiguration.addAttribute("key", ModuleKeyGenerator.generateKey(DESCRIPTOR_NAME));
        indexDocumentConfiguration.addAttribute("entity-key", bean.getPropertyType().getValue());
        indexDocumentConfiguration.addAttribute("i18n-name-key", bean.getName().getI18n());

        for (EntityPropertyIndexKeyConfigurationBean keyConfigurationBean : bean.getKeyConfigurations())
        {
            final Element keyConfigurationElement = indexDocumentConfiguration.addElement("key");
            keyConfigurationElement.addAttribute("property-key", keyConfigurationBean.getPropertyKey());

            for (EntityPropertyIndexExtractionConfigurationBean extractionBean : keyConfigurationBean.getExtractions())
            {
                final Element propertyExtractionElement = keyConfigurationElement.addElement("extract");
                propertyExtractionElement.addAttribute("path", extractionBean.getPath());
                propertyExtractionElement.addAttribute("type", extractionBean.getType().toString());
            }
        }

        EntityPropertyIndexDocumentModuleDescriptorImpl descriptor = autowireUtil.createBean(EntityPropertyIndexDocumentModuleDescriptorImpl.class);
        descriptor.init(plugin, indexDocumentConfiguration);

        return descriptor;
    }

}
