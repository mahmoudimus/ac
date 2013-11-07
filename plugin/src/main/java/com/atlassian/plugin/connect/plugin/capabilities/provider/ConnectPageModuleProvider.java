package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectPageCapabilityBeanAdapter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Module Provider for a Connect Page Module.
 * Note that there is actually no P2 module descriptor. Instead it is modelled as a web-item plus a servlet
 */
public class ConnectPageModuleProvider implements ConnectModuleProvider<ConnectPageCapabilityBean>
{
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFramePageServletDescriptorFactory servletDescriptorFactory;

    public ConnectPageModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                     IFramePageServletDescriptorFactory servletDescriptorFactory)
    {
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.servletDescriptorFactory = checkNotNull(servletDescriptorFactory);
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName,
                                                 List<ConnectPageCapabilityBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectPageCapabilityBean bean : beans)
        {
            ConnectPageCapabilityBeanAdapter adapter = createBeanAdapter(bean, jsonFieldName, plugin.getKey());
            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, adapter.getWebItemBean()));
            builder.add(servletDescriptorFactory.createIFrameServletDescriptor(plugin, adapter.getServletBean()));
        }

        return builder.build();
    }

    private ConnectPageCapabilityBeanAdapter createBeanAdapter(ConnectPageCapabilityBean bean, String jsonFieldName, String pluginKey)
    {
        //    private final String decorator; // e.g. "atl.general"
//    private final String templateSuffix; // e.g. "-project-admin". Note general page etc has "", dialogPage &
//    private final Map<String, String> metaTagsContent; // e.g. "adminActiveTab" -> bean.getKey()

        // TODO: Need to pass in decorator etc. Could use same trick as ConnectTabPanelModuleProvider altho not pretty
        return new ConnectPageCapabilityBeanAdapter(bean, pluginKey);
    }

}
