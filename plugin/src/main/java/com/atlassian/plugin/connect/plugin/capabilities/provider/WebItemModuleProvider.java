package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrl;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;

@Component
public class WebItemModuleProvider implements ConnectModuleProvider<WebItemCapabilityBean>
{
    private final WebItemModuleDescriptorFactory webItemFactory;
    private final RelativeAddOnUrlConverter relativeAddOnUrlConverter;

    @Autowired
    public WebItemModuleProvider(WebItemModuleDescriptorFactory webItemFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        this.webItemFactory = webItemFactory;
        this.relativeAddOnUrlConverter = relativeAddOnUrlConverter;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<WebItemCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (WebItemCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, WebItemCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        if (bean.isAbsolute() || bean.getContext().equals(AddOnUrlContext.product))
        {
            descriptors.add(webItemFactory.createModuleDescriptor(plugin, addonBundleContext,bean));
        }
        else
        {
            RelativeAddOnUrl localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getLink());
            
            WebItemCapabilityBean newBean = newWebItemBean(bean).withLink(localUrl.getRelativeUri()).build();
            descriptors.add(webItemFactory.createModuleDescriptor(plugin, addonBundleContext, newBean));

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
