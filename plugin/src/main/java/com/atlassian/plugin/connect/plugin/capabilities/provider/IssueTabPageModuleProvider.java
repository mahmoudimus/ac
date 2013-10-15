package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IssueTabPageModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.IssueTabPageCapabilityBean.newIssueTabPageBean;

@Component
public class IssueTabPageModuleProvider implements ConnectModuleProvider<IssueTabPageCapabilityBean>
{
    private final IssueTabPageModuleDescriptorFactory webItemFactory;
    private final RelativeAddOnUrlConverter relativeAddOnUrlConverter;

    @Autowired
    public IssueTabPageModuleProvider(IssueTabPageModuleDescriptorFactory webItemFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        this.webItemFactory = webItemFactory;
        this.relativeAddOnUrlConverter = relativeAddOnUrlConverter;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<IssueTabPageCapabilityBean> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (IssueTabPageCapabilityBean bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, IssueTabPageCapabilityBean bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        String localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getUrl());

        IssueTabPageCapabilityBean newBean = newIssueTabPageBean(bean).withUrl(localUrl).build();
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
//                descriptors.add(iframePageFactory.createIFrameServletDescriptor(plugin,newBean,localUrl,bean.getUrl(),"atl.general","", new AlwaysDisplayCondition(),new HashMap<String, String>()));
//            }

        return descriptors;
    }
}
