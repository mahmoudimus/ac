package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyIndexSchemaModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ContentPropertyIndexSchemaModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Representation of a group of extractions specified by an add-on.
 */
@ConfluenceComponent
public class DefaultContentPropertyIndexSchemaModuleProvider implements ContentPropertyIndexSchemaModuleProvider
{
    private final ContentPropertyIndexSchemaModuleDescriptorFactory factory;

    @Autowired
    public DefaultContentPropertyIndexSchemaModuleProvider(
            ContentPropertyIndexSchemaModuleDescriptorFactory factory)
    {
        this.factory = factory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin plugin,
            String jsonFieldName, List<ContentPropertyIndexSchemaModuleBean> beans)
    {
        return Lists.transform(beans, new Function<ContentPropertyIndexSchemaModuleBean, ModuleDescriptor>()
        {
            @Override
            public ContentPropertyIndexSchemaModuleDescriptor apply(
                    @Nullable ContentPropertyIndexSchemaModuleBean input)
            {
                return factory.createModuleDescriptor(moduleProviderContext, plugin, input);
            }
        });
    }
}
