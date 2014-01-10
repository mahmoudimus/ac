package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.EntityPropertyIndexDocumentModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectEntityPropertyIndexDocumentModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@JiraComponent
public class EntityPropertyIndexDocumentModuleProvider implements ConnectModuleProvider<EntityPropertyIndexDocumentModuleBean>
{
    private final ConnectEntityPropertyIndexDocumentModuleDescriptorFactory descriptorFactory;

    @Autowired
    public EntityPropertyIndexDocumentModuleProvider(ConnectEntityPropertyIndexDocumentModuleDescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final Plugin plugin, final BundleContext addonBundleContext,
            final String jsonFieldName, final List<EntityPropertyIndexDocumentModuleBean> beans)
    {
        return Lists.transform(beans, new Function<EntityPropertyIndexDocumentModuleBean, ModuleDescriptor>()
        {
            @Override
            public ModuleDescriptor apply(final EntityPropertyIndexDocumentModuleBean bean)
            {
                return descriptorFactory.createModuleDescriptor(plugin, addonBundleContext, bean);
            }
        });
    }

}
