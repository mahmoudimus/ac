package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.*;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.XWorkActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsContextInterceptor;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.springframework.beans.factory.annotation.Autowired;

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
public class SpaceToolsTabModuleProvider implements ConnectModuleProvider<SpaceToolsTabModuleBean>
{
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
    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<SpaceToolsTabModuleBean> beans)
    {
        List<ModuleDescriptor> modules = newArrayList();
        for (SpaceToolsTabModuleBean bean : beans)
        {
            XWorkActionModuleBean actionBean = createActionBean(addon, bean);
            modules.add(xWorkActionDescriptorFactory.create(addon, theConnectPlugin, actionBean));

            IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                    .addOn(addon.getKey())
                    .module(bean.getKey(addon))
                    .genericBodyTemplate()
                    .urlTemplate(bean.getUrl())
                    .build();

            iFrameRenderStrategyRegistry.register(addon.getKey(), bean.getRawKey(), renderStrategy);

            String actionUrl = actionBean.getUrl() + "?key=${space.key}";
            for (WebItemModuleBean webItemModuleBean : createWebItemBeans(bean, actionUrl))
            {
                modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin, webItemModuleBean));
            }
        }
        return modules;
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
}
