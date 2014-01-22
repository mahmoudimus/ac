package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrl;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

@Component
public class WebItemModuleProvider implements ConnectModuleProvider<WebItemModuleBean>
{
    private final WebItemModuleDescriptorFactory webItemFactory;
    

    @Autowired
    public WebItemModuleProvider(WebItemModuleDescriptorFactory webItemFactory)
    {
        this.webItemFactory = webItemFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<WebItemModuleBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebItemModuleBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, WebItemModuleBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        if (bean.isAbsolute() || bean.getContext().equals(AddOnUrlContext.product) || bean.getContext().equals(AddOnUrlContext.addon))
        {
            descriptors.add(webItemFactory.createModuleDescriptor(plugin, addonBundleContext, bean));
        }
        else
        {
            String localUrl = ConnectIFrameServlet.iFrameServletPath(plugin.getKey(),bean.getUrl());
            WebItemModuleBean newBean = newWebItemBean(bean).withUrl(localUrl).build();
            descriptors.add(webItemFactory.createModuleDescriptor(plugin, addonBundleContext, newBean));
        }

        return descriptors;
    }
}
