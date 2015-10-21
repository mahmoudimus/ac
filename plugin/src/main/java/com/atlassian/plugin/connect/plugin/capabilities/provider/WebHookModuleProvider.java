package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebHookModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebHookModuleProvider extends AbstractConnectCoreModuleProvider<WebHookModuleBean>
{

    private static final WebHookModuleMeta META = new WebHookModuleMeta();

    private ConnectWebHookModuleDescriptorFactory moduleDescriptorFactory;
    private WebHookScopeValidator webHookScopeValidator;

    @Autowired
    public WebHookModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            ConnectWebHookModuleDescriptorFactory moduleDescriptorFactory,
            WebHookScopeValidator webHookScopeValidator)
    {
        super(pluginRetrievalService, schemaValidator);
        this.moduleDescriptorFactory = moduleDescriptorFactory;
        this.webHookScopeValidator = webHookScopeValidator;
    }

    @Override
    public ConnectModuleMeta<WebHookModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<WebHookModuleBean> deserializeAddonDescriptorModules(String jsonModuleListEntry,
            ShallowConnectAddonBean descriptor) throws ConnectModuleValidationException
    {
        List<WebHookModuleBean> webhooks = super.deserializeAddonDescriptorModules(jsonModuleListEntry, descriptor);
        webHookScopeValidator.validate(descriptor, webhooks);
        return webhooks;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<WebHookModuleBean> modules, ConnectModuleProviderContext moduleProviderContext)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        for (WebHookModuleBean webhook : modules)
        {
            descriptors.add(moduleDescriptorFactory.createModuleDescriptor(moduleProviderContext,
                    pluginRetrievalService.getPlugin(), webhook));
        }
        return descriptors;
    }
}
