package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleMeta;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebHookModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.AbstractConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.module.ConnectModuleValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WebHookModuleProvider extends AbstractConnectModuleProvider<WebHookModuleBean>
{

    private static final WebHookModuleMeta META = new WebHookModuleMeta();

    private ConnectWebHookModuleDescriptorFactory connectWebHookModuleDescriptorFactory;
    private WebHookScopeValidator webHookScopeValidator;

    @Autowired
    public WebHookModuleProvider(ConnectWebHookModuleDescriptorFactory connectWebHookModuleDescriptorFactory,
                                 WebHookScopeValidator webHookScopeValidator)
    {
        this.connectWebHookModuleDescriptorFactory = connectWebHookModuleDescriptorFactory;
        this.webHookScopeValidator = webHookScopeValidator;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, List<WebHookModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();

        for (WebHookModuleBean bean: beans)
        {
            descriptors.addAll(beanToDescriptors(moduleProviderContext, theConnectPlugin, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(ConnectModuleProviderContext moduleProviderContext,
                                                                     Plugin theConnectPlugin, WebHookModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<>();
        descriptors.add(connectWebHookModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin, bean));

        return descriptors;
    }

    @Override
    public String getSchemaPrefix()
    {
        return "common";
    }

    @Override
    public ConnectModuleMeta<WebHookModuleBean> getMeta()
    {
        return META;
    }
    
    @Override
    public List<WebHookModuleBean> validate(String rawModules, Class<WebHookModuleBean> type, Plugin plugin, ShallowConnectAddonBean bean) throws ConnectModuleValidationException
    {
        List<WebHookModuleBean> webhooks = super.validate(rawModules, type, plugin, bean);
        webHookScopeValidator.validate(bean, webhooks);
        return webhooks;
    }
}
