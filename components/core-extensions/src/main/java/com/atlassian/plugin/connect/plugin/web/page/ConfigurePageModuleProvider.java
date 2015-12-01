package com.atlassian.plugin.connect.plugin.web.page;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.web.iframe.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.modules.beans.ConfigurePageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.spi.ProductAccessor;
import com.atlassian.plugin.connect.api.lifecycle.WebItemModuleDescriptorFactory;
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
            ConditionClassAccessor conditionClassAccessor,
            ConnectJsonSchemaValidator schemaValidator,
            ConditionLoadingValidator conditionLoadingValidator,
            ProductAccessor productAccessor)
    {
        super(pluginRetrievalService, iFrameRenderStrategyBuilderFactory, iFrameRenderStrategyRegistry,
                webItemModuleDescriptorFactory, conditionClassAccessor, schemaValidator, conditionLoadingValidator, productAccessor);
    }

    @Override
    public ConnectModuleMeta<ConnectPageModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<ConnectPageModuleBean> modules, ConnectAddonBean addon)
    {
        super.createPluginModuleDescriptors(modules, addon);

        List<ModuleDescriptor> descriptors = new ArrayList<>();
        Iterator<ConnectPageModuleBean> iterator = modules.iterator();
        if (iterator.hasNext())
        {
            ConnectPageModuleBean configurePage = iterator.next();
            ModuleDescriptor descriptor = new ConfigurePageModuleDescriptor();
            descriptor.init(pluginRetrievalService.getPlugin(), new DOMElement("connectConfigurePage").addAttribute("key",
                    configurePage.getKey(addon)));
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
