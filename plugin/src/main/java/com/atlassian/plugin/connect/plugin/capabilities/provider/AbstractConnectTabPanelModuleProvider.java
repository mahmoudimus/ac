package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectIssueTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectIssueTabPanelModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractConnectTabPanelModuleProvider<B extends ConnectIssueTabPanelCapabilityBean> implements ConnectModuleProvider<B>
{
    protected final RelativeAddOnUrlConverter relativeAddOnUrlConverter;
    private final ConnectIssueTabPanelModuleDescriptorFactory issueTabFactory;

    public AbstractConnectTabPanelModuleProvider(ConnectIssueTabPanelModuleDescriptorFactory issueTabFactory, RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        this.relativeAddOnUrlConverter = relativeAddOnUrlConverter;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<B> beans)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        for (B bean : beans)
        {
            descriptors.addAll(beanToDescriptors(plugin,addonBundleContext, bean));
        }

        return descriptors;
    }

    private Collection<? extends ModuleDescriptor> beanToDescriptors(Plugin plugin, BundleContext addonBundleContext, B bean)
    {
        List<ModuleDescriptor> descriptors = new ArrayList<ModuleDescriptor>();

        String localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getUrl());

        B newBean = createCapabilityBean(bean, localUrl);
        descriptors.add(createModuleDescriptor(plugin, addonBundleContext, newBean));

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

    protected abstract ModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, B newBean);

    protected abstract B createCapabilityBean(B bean, String localUrl);

    protected ModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, ConnectIssueTabPanelCapabilityBean newBean)
    {
        return issueTabFactory.createModuleDescriptor(plugin, addonBundleContext, newBean);
    }
}
