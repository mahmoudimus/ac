package com.atlassian.plugin.connect.plugin.lifecycle;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.spi.lifecycle.ConnectModuleProvider;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.dom4j.Element;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

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
    protected void provideValidationRules(ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("@class and string-length(@class) > 0")
                                .withError("A provider class must be specified via the 'class' attribute"));
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
