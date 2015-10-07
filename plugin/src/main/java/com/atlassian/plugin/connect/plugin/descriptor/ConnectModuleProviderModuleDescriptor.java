package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProvider;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.dom4j.Element;

public class ConnectModuleProviderModuleDescriptor extends AbstractModuleDescriptor<ConnectModuleProvider>
{
    private final ResettableLazyReference<ConnectModuleProvider> moduleLazyReference = new ResettableLazyReference<ConnectModuleProvider>()
    {
        @Override
        protected ConnectModuleProvider create() throws Exception
        {
            return moduleFactory.createModule(moduleClassName, ConnectModuleProviderModuleDescriptor.this);
        }
    };

    public ConnectModuleProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }
    
    @Override
    public void init(final Plugin plugin, final Element element)
    {
        super.init(plugin, element);
    }

    @Override
    public ConnectModuleProvider getModule()
    {
        return moduleLazyReference.get();
    }
}
