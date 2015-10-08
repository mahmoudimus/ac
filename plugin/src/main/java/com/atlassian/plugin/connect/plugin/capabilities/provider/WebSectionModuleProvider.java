package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebSectionModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebSectionModuleProvider extends AbstractConnectCoreModuleProvider<WebSectionModuleBean>
{

    private static final WebSectionModuleMeta META = new WebSectionModuleMeta();

    private final ConnectWebSectionModuleDescriptorFactory webSectionFactory;

    @Autowired
    public WebSectionModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ConnectWebSectionModuleDescriptorFactory webSectionFactory)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webSectionFactory = webSectionFactory;
    }

    @Override
    public ConnectModuleMeta<WebSectionModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebSectionModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebSectionModuleBean webSection : modules)
        {
            descriptors.add(webSectionFactory.createModuleDescriptor(moduleProviderContext,
                    pluginRetrievalService.getPlugin(), webSection));
        }
        return descriptors;
    }

}
