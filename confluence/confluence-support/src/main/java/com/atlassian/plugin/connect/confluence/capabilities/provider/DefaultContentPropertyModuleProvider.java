package com.atlassian.plugin.connect.confluence.capabilities.provider;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.contentproperty.ContentPropertyIndexSchemaModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Representation of a group of extractions specified by an add-on.
 */
@ConfluenceComponent
public class DefaultContentPropertyModuleProvider implements ContentPropertyModuleProvider
{
    private final ContentPropertyIndexSchemaModuleDescriptorFactory contentPropertyIndexFactory;

    @Autowired
    public DefaultContentPropertyModuleProvider(ContentPropertyIndexSchemaModuleDescriptorFactory factory)
    {
        this.contentPropertyIndexFactory = factory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext,
                                                 final Plugin plugin,
                                                 String jsonFieldName, List<ContentPropertyModuleBean> beans)
    {
        return Lists.transform(beans, new Function<ContentPropertyModuleBean, ModuleDescriptor>()
        {
            @Override
            public ContentPropertyIndexSchemaModuleDescriptor apply(
                    @Nullable ContentPropertyModuleBean input)
            {
                return contentPropertyIndexFactory.createModuleDescriptor(moduleProviderContext, plugin, input);
            }
        });
    }
}
