package com.atlassian.plugin.connect.plugin.capabilities.descriptor.contentproperty;

import com.atlassian.confluence.plugins.contentproperty.index.descriptor.ContentPropertyAliasModuleDescriptor;
import com.atlassian.confluence.plugins.contentproperty.index.schema.SchemaFieldType;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.UISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexFieldType;
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
public class ContentPropertyAliasModuleDescriptorFactory
{
    private static final Logger log = LoggerFactory.getLogger(ContentPropertyIndexSchemaModuleDescriptorFactory.class);

    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public ContentPropertyAliasModuleDescriptorFactory(ConnectContainerUtil connectContainerUtil)
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

                        String moduleKey = bean.getKey(connectAddonBean) + keyConfigurationBean.getPropertyKey() + "-" + extractionBean.getObjectName() + "-" + (++index);
                        String addonBaseUrl = connectAddonBean.getBaseUrl();

                        descriptor.setJsonExpression(extractionBean.getObjectName());
                        descriptor.init(plugin, createXmlConfig(extractionBean, moduleKey, addonBaseUrl));
                        descriptors.add(descriptor);
                    }
                }
            }
        }
        return descriptors;
    }

    private Element createXmlConfig(ContentPropertyIndexExtractionConfigurationBean extractionBean, String moduleKey, String addonBaseUrl)
    {
        Element aliasElement = new DOMElement("content-property-field-alias");
        aliasElement.addAttribute("key", moduleKey + "-field-alias");
        aliasElement.addAttribute("i18n-name-key", moduleKey);
        aliasElement.addAttribute("class", FieldHandler.class.getName());

        UISupportModuleBean uiSupport = extractionBean.getUiSupport();
        if (uiSupport != null)
        {
            Element uiSupportelement = new DOMElement("ui-support");
            uiSupportelement.addAttribute("value-type", extractionBean.getType().toString());
            if (uiSupport.getDefaultOperator() == null)
            {
                uiSupportelement.addAttribute("operator", extractionBean.getType() == ContentPropertyIndexFieldType.text ? "~" : "=");
                uiSupportelement.addAttribute("default-operator", extractionBean.getType() == ContentPropertyIndexFieldType.text ? "~" : "=");
            }
            else
            {
                uiSupportelement.addAttribute("operator", uiSupport.getDefaultOperator());
                uiSupportelement.addAttribute("default-operator", uiSupport.getDefaultOperator());
            }

            if (uiSupport.getName() != null)
            {
                uiSupportelement.addAttribute("i18n-key", uiSupport.getName().getKeyOrValue());
            }
            if (uiSupport.getTooltip() != null)
            {
                uiSupportelement.addAttribute("i18n-field-tooltip", uiSupport.getTooltip().getKeyOrValue());
            }

            String relativeDataUri = uiSupport.getDataUri();
            if (relativeDataUri != null)
            {
                uiSupportelement.addAttribute("data-uri", addonBaseUrl + uiSupport.getDataUri());
            }
            aliasElement.add(uiSupportelement);
        }

        if (log.isDebugEnabled())
        {
            log.debug(printNode(aliasElement));
        }
        return aliasElement;
    }


}
