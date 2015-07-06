package com.atlassian.plugin.connect.jira.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.api.iframe.servlet.ConnectIFrameServletPath;
import com.atlassian.plugin.connect.jira.condition.IsProjectAdminCondition;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.atlassian.plugin.connect.jira.capabilities.provider.JiraTemplateHelper.projectAdminTabTemplate;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;

/**
 * Module Provider for a Connect Project Admin TabPanel Module. Note that there is actually no P2 module descriptor.
 * Instead it is modelled as a web-item plus a servlet
 */
@JiraComponent
public class ConnectProjectAdminTabPanelModuleProvider extends ConnectModuleProvider
{
    public static final String DESCRIPTOR_KEY = "jiraProjectAdminTabPanels";
    public static final Class BEAN_CLASS = ConnectProjectAdminTabPanelModuleBean.class;    
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
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<JsonObject> modules)
    {
        ImmutableList.Builder<ModuleDescriptor> builder = ImmutableList.builder();

        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        for (JsonObject module: modules)
        {
            ConnectProjectAdminTabPanelModuleBean bean = new Gson().fromJson(module, ConnectProjectAdminTabPanelModuleBean.class);

            // render a web item for our tab
            WebItemModuleBean webItemModuleBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(bean.getRawKey())
                    .withUrl(ConnectIFrameServletPath.forModule(connectAddonBean.getKey(), bean.getRawKey()))
                    .withContext(AddOnUrlContext.page)
                    .withLocation(bean.getAbsoluteLocation())
                    .withWeight(bean.getWeight())
                    .withConditions(bean.getConditions())
                    .setNeedsEscaping(false)
                    .build();

            builder.add(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin,
                    webItemModuleBean, IsProjectAdminCondition.class));

            // register a render strategy for the servlet backing our iframe tab
            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .template(projectAdminTabTemplate())
                    .urlTemplate(bean.getUrl())
                    .additionalRenderContext(ADMIN_ACTIVE_TAB, bean.getKey(connectAddonBean))
                    .conditions(bean.getConditions())
                    .conditionClass(IsProjectAdminCondition.class)
                    .title(bean.getDisplayName())
                    .build();

            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);
        }

        return builder.build();
    }

    @Override
    public Class getBeanClass()
    {
        return BEAN_CLASS;
    }

    @Override
    public String getDescriptorKey()
    {
        return DESCRIPTOR_KEY;
    }

}
