package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.contentproperty.ContentPropertyIndexSchemaModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleMeta;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Representation of a group of extractions specified by an add-on.
 */
@ConfluenceComponent
public class DefaultContentPropertyModuleProvider extends ContentPropertyModuleProvider
{

    private static final ContentPropertyModuleMeta META = new ContentPropertyModuleMeta();

    private final ContentPropertyIndexSchemaModuleDescriptorFactory contentPropertyIndexFactory;

    @Autowired
    public DefaultContentPropertyModuleProvider(ContentPropertyIndexSchemaModuleDescriptorFactory factory)
    {
        this.contentPropertyIndexFactory = factory;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ContentPropertyModuleBean> modules, final Plugin plugin, final ConnectModuleProviderContext moduleProviderContext)
    {
        return Lists.transform(modules, bean -> contentPropertyIndexFactory.createModuleDescriptor(moduleProviderContext, plugin, bean));
    }

    @Override
    public String getSchemaPrefix()
    {
        return "confluence";
    }

    @Override
    public ConnectModuleMeta<ContentPropertyModuleBean> getMeta()
    {
        return META;
    }
}
