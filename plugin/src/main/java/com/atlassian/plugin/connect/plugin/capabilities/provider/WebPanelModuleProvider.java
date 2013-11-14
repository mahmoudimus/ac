package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebPanelConnectModuleDescriptorFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class WebPanelModuleProvider implements ConnectModuleProvider<WebPanelCapabilityBean>
{
    private final WebPanelConnectModuleDescriptorFactory webPanelFactory;

    @Autowired
    public WebPanelModuleProvider(WebPanelConnectModuleDescriptorFactory webPanelFactory)
    {
        this.webPanelFactory = webPanelFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<WebPanelCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebPanelCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin, addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, WebPanelCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        descriptors.add(webPanelFactory.createModuleDescriptor(plugin, addonBundleContext, bean));

        return descriptors;
    }
}
