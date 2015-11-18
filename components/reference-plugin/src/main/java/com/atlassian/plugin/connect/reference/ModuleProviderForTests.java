package com.atlassian.plugin.connect.reference;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.spi.descriptor.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.lifecycle.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.lifecycle.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModuleProviderForTests extends AbstractConnectPageModuleProvider
{
    @Autowired
    public ModuleProviderForTests(
            @ComponentImport PluginRetrievalService pluginRetrievalService,
            @ComponentImport IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            @ComponentImport IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            @ComponentImport WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            @ComponentImport PluginAccessor pluginAccessor)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pluginAccessor);
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return new ConnectModuleMeta<ConnectPageModuleBean>("testModules", ConnectPageModuleBean.class)
        {
        };
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<ConnectPageModuleBean> beans = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        if (beans.get(0).getRawKey().equals("bad"))
        {
            throw new ConnectModuleValidationException(descriptor, getMeta(), "Key is bad!", null, null);
        }
        return beans;
    }

    @Override
    protected String getDecorator()
    {
        return "placeholder";
    }

    @Override
    protected String getDefaultSection()
    {
        return "placeholder";
    }

    @Override
    protected int getDefaultWeight()
    {
        return 0;
    }
}
