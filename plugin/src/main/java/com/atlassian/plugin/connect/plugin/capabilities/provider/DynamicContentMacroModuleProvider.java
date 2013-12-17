package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

@ConfluenceComponent
public class DynamicContentMacroModuleProvider extends AbstractContentMacroModuleProvider<DynamicContentMacroModuleBean>
{
    private final DynamicContentMacroModuleDescriptorFactory dynamicContentMacroModuleDescriptorFactory;

    @Autowired
    public DynamicContentMacroModuleProvider(DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                             HostContainer hostContainer,
                                             AbsoluteAddOnUrlConverter urlConverter)
    {
        super(webItemModuleDescriptorFactory, hostContainer, urlConverter);
        this.dynamicContentMacroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(Plugin plugin, BundleContext bundleContext, DynamicContentMacroModuleBean macroBean)
    {
        return dynamicContentMacroModuleDescriptorFactory.createModuleDescriptor(plugin, bundleContext, macroBean);
    }
}
