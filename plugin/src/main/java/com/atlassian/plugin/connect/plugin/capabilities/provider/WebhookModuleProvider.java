package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebhookCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectWebhookModuleDescriptorFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WebhookModuleProvider  implements ConnectModuleProvider<WebhookCapabilityBean>
{
    private ConnectWebhookModuleDescriptorFactory connectWebhookModuleDescriptorFactory;

    @Autowired
    public WebhookModuleProvider(ConnectWebhookModuleDescriptorFactory connectWebhookModuleDescriptorFactory) {
        this.connectWebhookModuleDescriptorFactory = connectWebhookModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<WebhookCapabilityBean> beans) {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebhookCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, WebhookCapabilityBean bean) {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();
        descriptors.add(connectWebhookModuleDescriptorFactory.createModuleDescriptor(plugin,addonBundleContext,bean));

        return descriptors;
    }

}
