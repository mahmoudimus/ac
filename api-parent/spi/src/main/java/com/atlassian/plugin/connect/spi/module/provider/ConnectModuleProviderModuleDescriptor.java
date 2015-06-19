package com.atlassian.plugin.connect.spi.module.provider;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.dom4j.Element;

public class ConnectModuleProviderModuleDescriptor extends AbstractModuleDescriptor<TestConnectModuleProvider>
{
    private final ResettableLazyReference<TestConnectModuleProvider> moduleLazyReference = new ResettableLazyReference<TestConnectModuleProvider>()
    {
        @Override
        protected TestConnectModuleProvider create() throws Exception
        {
            System.out.println("HI WE'RE CREATING THE LAZY REFERENCE");
            return moduleFactory.createModule(moduleClassName, ConnectModuleProviderModuleDescriptor.this);
        }
    };

    public ConnectModuleProviderModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
        //moduleFactory.createModule(moduleClassName, ConnectModuleProviderModuleDescriptor.this);
        System.out.println("HI WE'RE CONSTRUCTING THE CONNECTMODULEPROVIDERMODULEDESCRIPTOR");
    }
    
    @Override
    public void init(final Plugin plugin, final Element element)
    {
        super.init(plugin, element);
        int i = 0;
        
    }

    @Override
    public TestConnectModuleProvider getModule()
    {
        System.out.println("HI WE'RE GETTING THE MODULE");
        return moduleLazyReference.get();
    }
}
