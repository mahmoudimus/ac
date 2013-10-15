package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebPanelModuleDescriptorFactory;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean.newWebPanelBean;

@Component
public class WebPanelModuleProvider implements ConnectModuleProvider<WebPanelCapabilityBean>
{
    private final WebPanelModuleDescriptorFactory WebPanelFactory;
    private final RelativeAddOnUrlConverter relativeAddOnUrlConverter;

    @Autowired
    public WebPanelModuleProvider(WebPanelModuleDescriptorFactory webPanelFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        this.WebPanelFactory = webPanelFactory;
        this.relativeAddOnUrlConverter = relativeAddOnUrlConverter;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<WebPanelCapabilityBean> beans)
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

        if (bean.isAbsolute() || bean.getContext().equals(AddOnUrlContext.product))
        {
            descriptors.add(WebPanelFactory.createModuleDescriptor(plugin, addonBundleContext,bean));
        }
        else
        {
            String localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getLink());
            
            WebPanelCapabilityBean newBean = newWebPanelBean(bean).withLink(localUrl).build();
            descriptors.add(WebPanelFactory.createModuleDescriptor(plugin, addonBundleContext, newBean));

            //todo: make sure we do something to actually look up condition and metaTags map
            //ONLY create the servlet if one doesn't already exist!!!
//            List<ServletModuleDescriptor> servletDescriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(ServletModuleDescriptor.class);
//            boolean servletExists = false;
//            for(ServletModuleDescriptor servletDescriptor : servletDescriptors)
//            {
//                if(servletDescriptor.getPaths().contains(localUrl))
//                {
//                    servletExists = true;
//                    break;
//                }
//            }
//            
//            if(!servletExists)
//            {
//                descriptors.add(iframePageFactory.createIFrameServletDescriptor(plugin,newBean,localUrl,bean.getLink(),"atl.general","", new AlwaysDisplayCondition(),new HashMap<String, String>()));
//            }
        }

        return descriptors;
    }
}
