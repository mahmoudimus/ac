package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.module.jira.conditions.IsProjectAdminCondition;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;

import com.google.common.collect.ImmutableList;

import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet.iFrameServletPath;

/**
 * Module Provider for a Connect Project Admin TabPanel Module. Note that there is actually no P2 module descriptor.
 * Instead it is modelled as a web-item plus a servlet
 */
@JiraComponent
public class ConnectProjectAdminTabPanelModuleProvider
        implements ConnectModuleProvider<ConnectProjectAdminTabPanelModuleBean>
{
    public static final String PROJECT_ADMIN_TAB_PANELS = "jiraProjectAdminTabPanels";

    private static final String ADMIN_ACTIVE_TAB = "adminActiveTab";

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public ConnectProjectAdminTabPanelModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<ConnectProjectAdminTabPanelModuleBean> beans)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        for (ConnectProjectAdminTabPanelModuleBean bean : beans)
        {
            // render a web item for our tab
            WebItemModuleBean webItemModuleBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(bean.getRawKey())
                    .withUrl(iFrameServletPath(addon.getKey(), bean.getRawKey()))
                    .withContext(AddOnUrlContext.page)
                    .withLocation(bean.getAbsoluteLocation())
                    .withWeight(bean.getWeight())
                    .withConditions(bean.getConditions())
                    .build();

            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin,
                    webItemModuleBean, IsProjectAdminCondition.class));

            // register a render strategy for the servlet backing our iframe tab
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(addon.getKey())
                    .module(bean.getKey(addon))
                    .projectAdminTabTemplate()
                    .urlTemplate(bean.getUrl())
                    .additionalRenderContext(ADMIN_ACTIVE_TAB, bean.getKey(addon))
                    .conditions(bean.getConditions())
                    .conditionClass(IsProjectAdminCondition.class)
                    .title(bean.getDisplayName())
                    .build();

            iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getRawKey(), renderStrategy);
        }

        return builder.build();
    }

}
