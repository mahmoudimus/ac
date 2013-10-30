package com.atlassian.plugin.connect.plugin.capabilities.descriptor;


import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebhookCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.ConnectAutowireUtil;
import com.atlassian.plugin.connect.plugin.capabilities.util.ModuleKeyGenerator;
import com.atlassian.webhooks.spi.provider.ModuleDescriptorWebHookListenerRegistry;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectWebhookModuleDescriptorFactory implements ConnectModuleDescriptorFactory<WebhookCapabilityBean,ConnectWebhookModuleDescriptor>
{
    private ParamsModuleFragmentFactory paramsModuleFragmentFactory;
    private ModuleDescriptorWebHookListenerRegistry moduleDescriptorWebHookListenerRegistry;

    @Autowired
    public ConnectWebhookModuleDescriptorFactory(ParamsModuleFragmentFactory paramsModuleFragmentFactory,
                                                 ModuleDescriptorWebHookListenerRegistry moduleDescriptorWebHookListenerRegistry) {
        this.paramsModuleFragmentFactory = paramsModuleFragmentFactory;
        this.moduleDescriptorWebHookListenerRegistry = moduleDescriptorWebHookListenerRegistry;
    }

    @Override
    public ConnectWebhookModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, WebhookCapabilityBean bean) {
        ConnectWebhookModuleDescriptor descriptor = new ConnectWebhookModuleDescriptor(moduleDescriptorWebHookListenerRegistry,
                bean, plugin.getKey());
        return descriptor;
    }
}
