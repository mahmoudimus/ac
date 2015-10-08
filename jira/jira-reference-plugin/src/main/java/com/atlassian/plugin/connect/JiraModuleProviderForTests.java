package com.atlassian.plugin.connect;

import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.capabilities.provider.AbstractConnectPageModuleProvider;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JiraModuleProviderForTests extends AbstractConnectPageModuleProvider
{
    @Autowired
    public JiraModuleProviderForTests(PluginRetrievalService pluginRetrievalService,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            PageConditionsFactory pageConditionsFactory)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, pageConditionsFactory);
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return new ConnectModuleMeta<ConnectPageModuleBean>()
        {
            @Override
            public boolean multipleModulesAllowed()
            {
                return true;
            }

            @Override
            public String getDescriptorKey()
            {
                return "jiraTestModules";
            }

            @Override
            public Class<ConnectPageModuleBean> getBeanClass()
            {
                return ConnectPageModuleBean.class;
            }
        };
    }

    @Override
    public List<ConnectPageModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry, ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<ConnectPageModuleBean> beans = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        if (beans.get(0).getRawKey().equals("bad"))
        {
            throw new ConnectModuleValidationException(getMeta().getDescriptorKey(), "Key is bad!");
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
        return "system.top.navigation.bar";
    }

    @Override
    protected int getDefaultWeight()
    {
        return 0;
    }
}
