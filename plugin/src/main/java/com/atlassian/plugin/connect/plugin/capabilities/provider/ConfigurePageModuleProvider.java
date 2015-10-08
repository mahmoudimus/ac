package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConfigurePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConfigurePageModuleDescriptor;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class ConfigurePageModuleProvider extends AbstractAdminPageModuleProvider
{

    private static final ConfigurePageModuleMeta META = new ConfigurePageModuleMeta();

    @Autowired
    public ConfigurePageModuleProvider(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            PageConditionsFactory pageConditionsFactory,
            ConnectJsonSchemaValidator schemaValidator,
            ProductAccessor productAccessor)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsFactory, schemaValidator, productAccessor);
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectPageModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        super.createPluginModuleDescriptors(modules, moduleProviderContext);

        List<ModuleDescriptor> descriptors = new ArrayList<>();
        Iterator<ConnectPageModuleBean> iterator = modules.iterator();
        if (iterator.hasNext())
        {
            ConnectPageModuleBean configurePage = iterator.next();
            ModuleDescriptor descriptor = new ConfigurePageModuleDescriptor();
            descriptor.init(pluginRetrievalService.getPlugin(), new DOMElement("connectConfigurePage").addAttribute("key",
                    configurePage.getKey(moduleProviderContext.getConnectAddonBean())));
            descriptors.add(descriptor);
        }
        return descriptors;
    }

    @Override
    protected boolean hasWebItem()
    {
        return false;
    }
}
