package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.contentproperty.ContentPropertyIndexSchemaModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.contentproperty.CqlFieldModuleDescriptorFactory;
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
    private final ContentPropertyIndexSchemaModuleDescriptorFactory cpFactory;
    private final CqlFieldModuleDescriptorFactory cfFactory;

    @Autowired
    public DefaultContentPropertyModuleProvider(
            ContentPropertyIndexSchemaModuleDescriptorFactory cpFactory,
            CqlFieldModuleDescriptorFactory cfFactory)
    {
        this.cpFactory = cpFactory;
        this.cfFactory = cfFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin plugin,
                                                 String jsonFieldName, List<ContentPropertyModuleBean> beans)
    {
        final List<ModuleDescriptor> result = new ArrayList<>();
        result.addAll(Lists.transform(beans, new Function<ContentPropertyModuleBean, ModuleDescriptor>()
        {
            @Override
            public ContentPropertyIndexSchemaModuleDescriptor apply(
                    @Nullable ContentPropertyModuleBean input)
            {
                return cpFactory.createModuleDescriptor(moduleProviderContext, plugin, input);
            }
        }));
        result.addAll(cfFactory.createModuleDescriptors(moduleProviderContext, plugin, beans));
        return result;
    }
}
