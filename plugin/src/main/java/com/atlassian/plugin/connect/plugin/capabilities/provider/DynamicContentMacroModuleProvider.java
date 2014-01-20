package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
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
                                             IFramePageServletDescriptorFactory servletDescriptorFactory,
                                             HostContainer hostContainer,
                                             AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter,
                                             RelativeAddOnUrlConverter relativeAddOnUrlConverter,
                                             I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        super(webItemModuleDescriptorFactory, servletDescriptorFactory, hostContainer,
                absoluteAddOnUrlConverter, relativeAddOnUrlConverter, i18nPropertiesPluginManager);
        this.dynamicContentMacroModuleDescriptorFactory = macroModuleDescriptorFactory;
    }

    @Override
    protected ModuleDescriptor createMacroModuleDescriptor(Plugin plugin, BundleContext bundleContext, DynamicContentMacroModuleBean macroBean)
    {
        return dynamicContentMacroModuleDescriptorFactory.createModuleDescriptor(plugin, bundleContext, macroBean);
    }
}
