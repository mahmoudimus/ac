package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.confluence.plugins.contentproperty.index.config.ContentPropertyIndexSchemaModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.ContentPropertyIndexSchemaModuleDescriptorFactory;
import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Representation of a group of extractions specified by an add-on.
 */
@ConfluenceComponent
public class DefaultContentPropertyModuleProvider extends ContentPropertyModuleProvider
{
    public static final String DESCRIPTOR_KEY = "confluenceContentProperties";
    public static final Class BEAN_CLASS = ContentPropertyModuleBean.class;
    
    private final ContentPropertyIndexSchemaModuleDescriptorFactory factory;

    @Autowired
    public DefaultContentPropertyModuleProvider(
            ContentPropertyIndexSchemaModuleDescriptorFactory factory)
    {
        this.factory = factory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<ContentPropertyModuleBean> beans)
    {
        return Lists.transform(beans, new Function<ContentPropertyModuleBean, ModuleDescriptor>()
        {
            @Override
            public ContentPropertyIndexSchemaModuleDescriptor apply(
                    @Nullable ContentPropertyModuleBean bean)
            {
                return factory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean);
            }
        });
    }

    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }

    @Override
    public String getSchemaPrefix()
    {
        return "confluence";
    }
}
