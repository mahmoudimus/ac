package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.PostInstallPageModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.PostInstallPageModuleDescriptor;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.google.common.collect.ImmutableList;
import org.dom4j.dom.DOMElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PostInstallPageModuleProvider extends AbstractGeneralPageModuleProvider
{

    private static final PostInstallPageModuleMeta META = new PostInstallPageModuleMeta();

    @Autowired
    public PostInstallPageModuleProvider(IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                         IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                         WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                         PageConditionsFactory pageConditionsFactory,
                                         ProductAccessor productAccessor)
    {
        super(iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry, webItemModuleDescriptorFactory,
                pageConditionsFactory, productAccessor);
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<ConnectPageModuleBean> beans)
    {
        super.provideModules(moduleProviderContext, theConnectPlugin, beans);

        if(null != beans && !beans.isEmpty())
        {
            ConnectPageModuleBean postInstallBean = beans.get(0);
            ModuleDescriptor descriptor = new PostInstallPageModuleDescriptor();
            descriptor.init(theConnectPlugin, new DOMElement("connectPostInstallPage").addAttribute("key",
                    postInstallBean.getKey(moduleProviderContext.getConnectAddonBean())));

            return ImmutableList.of(descriptor);
        }

        return Collections.emptyList();
    }

    @Override
    public String getSchemaPrefix()
    {
        return "common";
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return META;
    }

    @Override
    protected boolean hasWebItem()
    {
        return false;
    }

}
