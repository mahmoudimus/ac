package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Module Provider for a Connect Project Admin TabPanel Module.
 * Note that there is actually no P2 module descriptor. Instead it is modelled as a web-item plus a servlet
 */
public class ConnectProjectAdminTabPanelModuleProvider implements ConnectModuleProvider<ConnectProjectAdminTabPanelCapabilityBean>
{
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFramePageServletDescriptorFactory servletDescriptorFactory;
    private final RelativeAddOnUrlConverter relativeAddOnUrlConverter;

    public ConnectProjectAdminTabPanelModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                                     IFramePageServletDescriptorFactory servletDescriptorFactory,
                                                     RelativeAddOnUrlConverter relativeAddOnUrlConverter)
    {
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.servletDescriptorFactory = checkNotNull(servletDescriptorFactory);
        this.relativeAddOnUrlConverter = checkNotNull(relativeAddOnUrlConverter);
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, List<ConnectProjectAdminTabPanelCapabilityBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectProjectAdminTabPanelCapabilityBean bean : beans)
        {
            String localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getUrl());

            WebItemCapabilityBean webItemCapabilityBean = createWebItemCapabilityBean(bean, localUrl);
            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemCapabilityBean));

            // TODO: what is the right decorator???
            builder.add(servletDescriptorFactory.createIFrameServletDescriptor(plugin, webItemCapabilityBean, localUrl,
                    bean.getUrl(), "atl.general", "", new AlwaysDisplayCondition(), ImmutableMap.<String, String>of()));
        }

        return builder.build();
    }

    private WebItemCapabilityBean createWebItemCapabilityBean(ConnectProjectAdminTabPanelCapabilityBean bean,
                                                              String localUrl)
    {
        return newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withLink(localUrl)
                .withLocation(bean.getLocation())
                .withWeight(bean.getWeight())
                .build();
    }
}
