package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.contentproperty.ContentPropertyIndexSchemaModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Representation of a group of extractions specified by an add-on.
 */
@ConfluenceComponent
public class ContentPropertyModuleProvider extends AbstractConfluenceConnectModuleProvider<ContentPropertyModuleBean>
{

    private static final ContentPropertyModuleMeta META = new ContentPropertyModuleMeta();

    private final ContentPropertyIndexSchemaModuleDescriptorFactory contentPropertyIndexFactory;

    @Autowired
    public ContentPropertyModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ContentPropertyIndexSchemaModuleDescriptorFactory factory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.contentPropertyIndexFactory = factory;
    }

    @Override
    public ConnectModuleMeta<ContentPropertyModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ContentPropertyModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        return Lists.transform(modules, bean -> contentPropertyIndexFactory.createModuleDescriptor(moduleProviderContext,
                pluginRetrievalService.getPlugin(), bean));
    }
}
