package com.atlassian.plugin.connect.plugin.capabilities.descriptor.contentproperty;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectContainerUtil;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.querylang.plugins.AQLFieldModuleDescriptor;
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
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (ContentPropertyModuleBean bean : beans)
        {
            for (ContentPropertyIndexKeyConfigurationBean keyConfigurationBean : bean.getKeyConfigurations())
            {
                int index = 0;
                for (ContentPropertyIndexExtractionConfigurationBean extractionBean : keyConfigurationBean.getExtractions())
                {

                    AQLFieldModuleDescriptor  descriptor = connectContainerUtil.createBean
                            (AQLFieldModuleDescriptor .class);
                    descriptor.init(plugin, createXmlConfig(moduleProviderContext, bean, extractionBean, index++));
                    descriptors.add(descriptor);

                }
            }
        }
        return descriptors;
    }

    private Element createXmlConfig(ConnectModuleProviderContext moduleProviderContext,
                                    ContentPropertyModuleBean bean,
                                    ContentPropertyIndexExtractionConfigurationBean extractionBean,
                                    int index)
    {
        ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        Element indexSchema = new DOMElement("cql-field");
        indexSchema.addAttribute("key", bean.getKey(connectAddonBean)+"-cql-field-"+index);
        indexSchema.addAttribute("fieldName", extractionBean.getAlias());
//        indexSchema.addAttribute("name", extractionBean.getL);
        indexSchema.addAttribute("class", "");

        logSchema(indexSchema);
        return indexSchema;
    }

    private void logSchema(Element indexSchema)
    {
        if (log.isDebugEnabled())
        {
            log.debug(printNode(indexSchema));
        }
    }
}
