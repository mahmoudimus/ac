package com.atlassian.plugin.connect.confluence.contenttype;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.confluence.AbstractConfluenceConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleMeta;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class ExtensibleContentTypeModuleProvider
        extends AbstractConfluenceConnectModuleProvider<ExtensibleContentTypeModuleBean>
{
    private static final ExtensibleContentTypeModuleMeta META = new ExtensibleContentTypeModuleMeta();

    private final ExtensibleContentTypeModuleDescriptorFactory extensibleContentTypeModuleDescriptorFactory;
    private final ContentPropertyExtractorModuleDescriptorFactory contentPropertyExtractorModuleDescriptorFactory;
    private final ContentPropertyChangeExtractorModuleDescriptorFactory contentPropertyChangeExtractorModuleDescriptorFactory;

    @Autowired
    public ExtensibleContentTypeModuleProvider(
            PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ExtensibleContentTypeModuleDescriptorFactory extensibleContentTypeModuleDescriptorFactory,
            ContentPropertyExtractorModuleDescriptorFactory contentPropertyExtractorModuleDescriptorFactory,
            ContentPropertyChangeExtractorModuleDescriptorFactory contentPropertyChangeExtractorModuleDescriptorFactory)
    {
        super(pluginRetrievalService, schemaValidator);

        this.extensibleContentTypeModuleDescriptorFactory = extensibleContentTypeModuleDescriptorFactory;
        this.contentPropertyExtractorModuleDescriptorFactory = contentPropertyExtractorModuleDescriptorFactory;
        this.contentPropertyChangeExtractorModuleDescriptorFactory = contentPropertyChangeExtractorModuleDescriptorFactory;
    }

    @Override
    public ConnectModuleMeta<ExtensibleContentTypeModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ExtensibleContentTypeModuleBean> modules, ConnectAddonBean addon)
    {
        Plugin plugin = pluginRetrievalService.getPlugin();
        List<ModuleDescriptor> descriptors = Lists.newArrayList();

        for (ExtensibleContentTypeModuleBean bean : modules)
        {
            descriptors.add(extensibleContentTypeModuleDescriptorFactory.createModuleDescriptor(bean, addon, plugin));

            if (bean.getApiSupport().getIndexing().isEnabled())
            {
                descriptors.add(contentPropertyExtractorModuleDescriptorFactory.createModuleDescriptor(bean, addon, plugin));
                descriptors.add(contentPropertyChangeExtractorModuleDescriptorFactory.createModuleDescriptor(bean, addon, plugin));
            }
        }

        return descriptors;
    }
}
