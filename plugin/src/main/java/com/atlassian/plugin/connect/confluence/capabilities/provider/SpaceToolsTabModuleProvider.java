package com.atlassian.plugin.connect.confluence.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.confluence.ConfluenceConditions;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.XWorkActionDescriptorFactory;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.confluence.iframe.SpaceToolsTabContext;
import com.atlassian.plugin.connect.confluence.iframe.context.SpaceToolsContextInterceptor;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProvider;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean.newXWorkActionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean.newXWorkInterceptorBean;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Confluence "Space Tools" tabs are modelled as two web item sand an x-work action.
 * One web item is for the aforementioned Space Tools section, which appears for Modern Theme (5.0+) spaces.
 * The other web item is for the legacy Space Admin section, which appears for Documentation Theme spaces.
 */
@ConfluenceComponent
public class SpaceToolsTabModuleProvider extends ConnectModuleProvider
{
    public static final String DESCRIPTOR_KEY = "spaceToolsTabs";
    public static final Class BEAN_CLASS = SpaceToolsTabModuleBean.class;
    
    @VisibleForTesting
    public static final String SPACE_TOOLS_SECTION = "system.space.tools";
    @VisibleForTesting
    public static final String SPACE_ADMIN_SECTION = "system.space.admin";
    @VisibleForTesting
    public static final String DEFAULT_LOCATION = "addons";
    @VisibleForTesting
    public static final String LEGACY_LOCATION = "spaceops";    // All legacy web items go in this location.
    @VisibleForTesting
    public static final String SPACE_ADMIN_KEY_SUFFIX = "-legacy-space-admin";

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final XWorkActionDescriptorFactory xWorkActionDescriptorFactory;
    private final ProductAccessor productAccessor;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public SpaceToolsTabModuleProvider(final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            final XWorkActionDescriptorFactory xWorkActionDescriptorFactory, ProductAccessor productAccessor,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.xWorkActionDescriptorFactory = xWorkActionDescriptorFactory;
        this.productAccessor = productAccessor;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public List<ModuleDescriptor> provideModules(final ConnectModuleProviderContext moduleProviderContext, final Plugin theConnectPlugin, List<JsonObject> modules)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        List<ModuleDescriptor> moduleDescriptors = newArrayList();
        for (JsonObject module : modules)
        {
            SpaceToolsTabModuleBean bean = new Gson().fromJson(module, SpaceToolsTabModuleBean.class);
            XWorkActionModuleBean actionBean = createActionBean(connectAddonBean, bean);
            moduleDescriptors.add(xWorkActionDescriptorFactory.create(connectAddonBean, theConnectPlugin, actionBean));

            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(connectAddonBean.getKey())
                    .module(bean.getKey(connectAddonBean))
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .build();

            iFrameRenderStrategyRegistry.register(connectAddonBean.getKey(), bean.getRawKey(), renderStrategy);

            String actionUrl = actionBean.getUrl() + "?key=${space.key}";
            for (WebItemModuleBean webItemModuleBean : createWebItemBeans(bean, actionUrl))
            {
                moduleDescriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, theConnectPlugin,
                        webItemModuleBean));
            }
        }
        return moduleDescriptors;
    }

    private XWorkActionModuleBean createActionBean(ConnectAddonBean addon, SpaceToolsTabModuleBean bean)
    {
        String spaceAdminLegacyKey = bean.getRawKey() + SPACE_ADMIN_KEY_SUFFIX;
        SpaceToolsTabContext spaceTabContext = new SpaceToolsTabContext(addon.getKey(), bean.getRawKey(),
                bean.getDisplayName(), spaceAdminLegacyKey);

        return newXWorkActionBean()
                .withName(bean.getName())
                .withNamespace("/plugins/atlassian-connect/" + addon.getKey())
                .withClazz(SpaceToolsIFrameAction.class)
                .withParameter("context", spaceTabContext)
                .withDefaultValidatingInterceptorStack()
                .withInterceptor(newXWorkInterceptorBean()
                        .withName("space-context")
                        .withClazz(SpaceToolsContextInterceptor.class)
                        .build())
                .withVelocityResult("success", "/velocity/confluence/space-tab-page.vm")
                .build();

    }

    private List<WebItemModuleBean> createWebItemBeans(SpaceToolsTabModuleBean bean, String actionUrl)
    {
        Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
        String location = isNullOrEmpty(bean.getLocation()) ? DEFAULT_LOCATION : bean.getLocation();

        List<ConditionalBean> conditions = bean.getConditions();
        if (conditions == null)
        {
            conditions = newArrayList();
        }

        // These are the properties the standard and legacy web items have in common.
        WebItemModuleBean baseWebItemBean = newWebItemBean()
                .withName(bean.getName())
                .withUrl(actionUrl)
                .withConditions(conditions)
                .withWeight(weight)
                .build();

        WebItemModuleBean spaceToolsWebItemBean = newWebItemBean(baseWebItemBean)
                .withKey(bean.getRawKey())
                .withName(bean.getName())
                .withContext(AddOnUrlContext.product)
                .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                .withConditions(newSingleConditionBean()
                        .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                        .build())
                .setNeedsEscaping(false)
                .build();

        WebItemModuleBean spaceAdminWebItemBean = newWebItemBean(baseWebItemBean)
                .withKey(bean.getRawKey() + SPACE_ADMIN_KEY_SUFFIX)
                .withName(bean.getName())
                .withContext(AddOnUrlContext.product)
                .withLocation(SPACE_ADMIN_SECTION + "/" + LEGACY_LOCATION)
                .withConditions(newSingleConditionBean()
                        .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                        .withInvert(true)
                        .build())
                .build();

        return ImmutableList.of(spaceToolsWebItemBean, spaceAdminWebItemBean);
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
