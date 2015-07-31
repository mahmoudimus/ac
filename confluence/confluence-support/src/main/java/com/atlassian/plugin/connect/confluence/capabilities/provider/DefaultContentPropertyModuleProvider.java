package com.atlassian.plugin.connect.confluence.capabilities.provider;

import java.util.ArrayList;
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
    private final ContentPropertyIndexSchemaModuleDescriptorFactory cpIndexFactory;

    @Autowired
    public DefaultContentPropertyModuleProvider(ContentPropertyIndexSchemaModuleDescriptorFactory cpIndexFactory)
    {
        this.cpIndexFactory = cpIndexFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext,
                                                 final Plugin plugin,
                                                 String jsonFieldName, List<ContentPropertyModuleBean> beans)
    {
        final List<ModuleDescriptor> result = new ArrayList<>();
        result.addAll(Lists.transform(beans, new Function<ContentPropertyModuleBean, ModuleDescriptor>()
        {
            @Override
            public ContentPropertyIndexSchemaModuleDescriptor apply(
                    @Nullable ContentPropertyModuleBean input)
            {
                return cpIndexFactory.createModuleDescriptor(moduleProviderContext, plugin, input);
            }
        }));
        return result;
    }
}
