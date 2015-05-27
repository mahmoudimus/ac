package com.atlassian.plugin.connect.plugin.capabilities.descriptor.contentproperty;

import com.atlassian.confluence.plugins.contentproperty.index.descriptor.ContentPropertyAliasModuleDescriptor;
import com.atlassian.confluence.plugins.contentproperty.index.schema.SchemaFieldType;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.querylang.fields.FieldHandler;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;

@ConfluenceComponent
public class CqlFieldModuleDescriptorFactory
{
    private static final Logger log = LoggerFactory.getLogger(ContentPropertyIndexSchemaModuleDescriptorFactory.class);

    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public CqlFieldModuleDescriptorFactory(ConnectContainerUtil connectContainerUtil)
    {
        this.connectContainerUtil = connectContainerUtil;
    }

    public Collection<? extends ModuleDescriptor> createModuleDescriptors(ConnectModuleProviderContext moduleProviderContext,
                                                                          Plugin plugin,
                                                                          List<ContentPropertyModuleBean> beans)
    {
        ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();

        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (ContentPropertyModuleBean bean : beans)
        {
            for (ContentPropertyIndexKeyConfigurationBean keyConfigurationBean : bean.getKeyConfigurations())
            {
                int index = 0;
                for (ContentPropertyIndexExtractionConfigurationBean extractionBean : keyConfigurationBean.getExtractions())
                {
                    if (extractionBean.getAlias() != null)
                    {
                        ContentPropertyAliasModuleDescriptor descriptor = connectContainerUtil.createBean(ContentPropertyAliasModuleDescriptor.class);
                        descriptor.setAliasName(extractionBean.getAlias());
                        descriptor.setPropertyKey(keyConfigurationBean.getPropertyKey());
                        switch (extractionBean.getType())
                        {
                            case number:
                                descriptor.setType(SchemaFieldType.NUMBER);
                                break;
                            case date:
                                descriptor.setType(SchemaFieldType.DATE);
                                break;
                            case string:
                                descriptor.setType(SchemaFieldType.STRING);
                                break;
                            case text:
                                descriptor.setType(SchemaFieldType.TEXT);
                                break;
                        }
                        descriptor.setJsonExpression(extractionBean.getObjectName());
                        descriptor.init(plugin, createXmlConfig(bean.getKey(connectAddonBean) + keyConfigurationBean.getPropertyKey() + "-" + extractionBean.getObjectName() + "-" + (++index)));
                        descriptors.add(descriptor);
                    }
                }
            }
        }
        return descriptors;
    }

    private Element createXmlConfig(String moduleKey)
    {
        Element aliasElement = new DOMElement("content-property-field-alias");
        aliasElement.addAttribute("key", moduleKey + "-field-alias");
        aliasElement.addAttribute("i18n-name-key", moduleKey);
        aliasElement.addAttribute("class", FieldHandler.class.getName());
        if (log.isDebugEnabled())
        {
            log.debug(printNode(aliasElement));
        }
        return aliasElement;
    }


}
