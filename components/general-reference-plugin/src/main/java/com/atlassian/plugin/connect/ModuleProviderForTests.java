package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.spi.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.lifecycle.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.web.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModuleProviderForTests extends AbstractConnectPageModuleProvider
{
    protected final ProductAccessor productAccessor;

    @Autowired
    public ModuleProviderForTests(PluginRetrievalService pluginRetrievalService,
                                  IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
                                  IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
                                  WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                  PageConditionsFactory pageConditionsFactory,
                                  ProductAccessor productAccessor)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsFactory);
        this.productAccessor = productAccessor;
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return new ConnectModuleMeta<ConnectPageModuleBean>("testModules", ConnectPageModuleBean.class) {};
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<ConnectPageModuleBean> beans = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        if (beans.get(0).getRawKey().equals("bad"))
        {
            throw new ConnectModuleValidationException(getMeta(), "Key is bad!");
        }
        return beans;
    }

    @Override
    protected String getDecorator()
    {
        return "atl.general";
    }

    @Override
    protected String getDefaultSection()
    {
        return productAccessor.getPreferredGeneralSectionKey();
    }

    @Override
    protected int getDefaultWeight()
    {
        return 0;
    }
}
