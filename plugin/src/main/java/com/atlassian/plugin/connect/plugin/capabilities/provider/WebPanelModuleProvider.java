package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebPanelConnectModuleDescriptorFactory;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean.newWebPanelBean;

@Component
public class WebPanelModuleProvider implements ConnectModuleProvider<WebPanelCapabilityBean>
{
    private final WebPanelConnectModuleDescriptorFactory webPanelFactory;
    private final RelativeAddOnUrlConverter relativeAddOnUrlConverter;
    private final IFramePageServletDescriptorFactory iFramePageServletDescriptorFactory;

    @Autowired
    public WebPanelModuleProvider(WebPanelConnectModuleDescriptorFactory webPanelFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter, IFramePageServletDescriptorFactory iFramePageServletDescriptorFactory)
    {
        this.webPanelFactory = webPanelFactory;
        this.relativeAddOnUrlConverter = relativeAddOnUrlConverter;
        this.iFramePageServletDescriptorFactory = iFramePageServletDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<WebPanelCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebPanelCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, WebPanelCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        if (bean.isAbsolute())
        {
            descriptors.add(webPanelFactory.createModuleDescriptor(plugin, addonBundleContext, bean));
        }
        else
        {
            String localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getUrl());
            
            WebPanelCapabilityBean newBean = newWebPanelBean(bean).withUrl(localUrl).build();
            descriptors.add(webPanelFactory.createModuleDescriptor(plugin, addonBundleContext, newBean));
            descriptors.add(iFramePageServletDescriptorFactory.createIFrameServletDescriptor(plugin, newBean, localUrl, bean.getUrl(), "atl.general", "", new AlwaysDisplayCondition(), new HashMap<String, String>()));
        }

        return descriptors;
    }
}
