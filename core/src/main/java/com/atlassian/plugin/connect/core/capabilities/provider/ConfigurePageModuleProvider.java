package com.atlassian.plugin.connect.core.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.core.capabilities.descriptor.ConfigurePageModuleDescriptor;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.google.common.collect.ImmutableList;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ConfigurePageModuleProvider extends AbstractAdminPageModuleProvider
{
    @Autowired
    public ConfigurePageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory,
                productAccessor);
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, String jsonFieldName, List<ConnectPageModuleBean> beans)
    {
        super.provideModules(moduleProviderContext, theConnectPlugin, jsonFieldName, beans);

        if(null != beans && !beans.isEmpty())
        {
            ConnectPageModuleBean configBean = beans.get(0);

            ModuleDescriptor descriptor = new ConfigurePageModuleDescriptor();
            descriptor.init(theConnectPlugin, new DOMElement("connectConfigurePage").addAttribute("key",
                    configBean.getKey(moduleProviderContext.getConnectAddonBean())));

            return ImmutableList.of(descriptor);
        }
        
        return Collections.EMPTY_LIST;
    }

    @Override
    protected boolean hasWebItem()
    {
        return false;
    }

}
