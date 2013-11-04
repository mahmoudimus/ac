package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectProjectAdminTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.IFramePageServletDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrl;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.IsProjectAdminCondition;
import com.atlassian.plugin.web.Condition;
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
    public static final String PROJECT_ADMIN_TAB_PANELS = "jiraProjectAdminTabPanels";
    private static final String TEMPLATE_SUFFIX = "-project-admin";
    private static final String ADMIN_ACTIVE_TAB = "adminActiveTab";
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFramePageServletDescriptorFactory servletDescriptorFactory;
    private final RelativeAddOnUrlConverter relativeAddOnUrlConverter;
    private final Condition condition;

    public ConnectProjectAdminTabPanelModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                                     IFramePageServletDescriptorFactory servletDescriptorFactory,
                                                     RelativeAddOnUrlConverter relativeAddOnUrlConverter, JiraAuthenticationContext authenticationContext)
    {
        this.webItemModuleDescriptorFactory = checkNotNull(webItemModuleDescriptorFactory);
        this.servletDescriptorFactory = checkNotNull(servletDescriptorFactory);
        this.relativeAddOnUrlConverter = checkNotNull(relativeAddOnUrlConverter);
        this.condition = new IsProjectAdminCondition(checkNotNull(authenticationContext));
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<ConnectProjectAdminTabPanelCapabilityBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectProjectAdminTabPanelCapabilityBean bean : beans)
        {
            RelativeAddOnUrl localUrl = relativeAddOnUrlConverter.addOnUrlToLocalServletUrl(plugin.getKey(), bean.getUrl());

            // we can't pass projectKey as an "extra param" to relativeAddOnUrlConverter as it will encode the ${}
            String webItemUri = localUrl.getRelativeUri() + "?projectKey=${project.key}";
            WebItemCapabilityBean webItemCapabilityBean = createWebItemCapabilityBean(bean,
                    webItemUri);
            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemCapabilityBean));

            builder.add(servletDescriptorFactory.createIFrameProjectConfigTabServletDescriptor(plugin,
                    webItemCapabilityBean, localUrl.getServletDescriptorUrl(), bean.getUrl(), "", TEMPLATE_SUFFIX,
                    condition, ImmutableMap.<String, String>of(ADMIN_ACTIVE_TAB, bean.getKey())));
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
                .withLocation(bean.getAbsoluteLocation())
                .withWeight(bean.getWeight())
                .build();
    }
}
