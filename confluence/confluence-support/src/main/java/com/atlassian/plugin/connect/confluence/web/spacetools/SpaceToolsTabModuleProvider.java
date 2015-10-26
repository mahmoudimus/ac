package com.atlassian.plugin.connect.confluence.web.spacetools;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.api.descriptor.ConnectJsonSchemaValidator;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.api.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.confluence.web.ConfluenceConditions;
import com.atlassian.plugin.connect.confluence.AbstractConfluenceConnectModuleProvider;
import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleMeta;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.spi.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.module.ConnectModuleProviderContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
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
public class SpaceToolsTabModuleProvider extends AbstractConfluenceConnectModuleProvider<SpaceToolsTabModuleBean>
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

    private static final SpaceToolsTabModuleMeta META = new SpaceToolsTabModuleMeta();

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final XWorkActionDescriptorFactory xWorkActionDescriptorFactory;
    private final ProductAccessor productAccessor;
    private final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;
    private final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;

    @Autowired
    public SpaceToolsTabModuleProvider(PluginRetrievalService pluginRetrievalService,
            ConnectJsonSchemaValidator schemaValidator,
            WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            XWorkActionDescriptorFactory xWorkActionDescriptorFactory, ProductAccessor productAccessor,
            IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory,
            IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry)
    {
        super(pluginRetrievalService, schemaValidator);
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.xWorkActionDescriptorFactory = xWorkActionDescriptorFactory;
        this.productAccessor = productAccessor;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
    }

    @Override
    public ConnectModuleMeta<SpaceToolsTabModuleBean> getMeta()
    {
        return META;
    }

    @Override
    public List<ModuleDescriptor> createPluginModuleDescriptors(List<SpaceToolsTabModuleBean> modules, final ConnectModuleProviderContext moduleProviderContext)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        Plugin plugin = pluginRetrievalService.getPlugin();
        List<ModuleDescriptor> moduleDescriptors = newArrayList();
        for (SpaceToolsTabModuleBean bean : modules)
        {
            XWorkActionModuleBean actionBean = createActionBean(connectAddonBean, bean);
            moduleDescriptors.add(xWorkActionDescriptorFactory.create(connectAddonBean, plugin, actionBean));

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
                moduleDescriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(moduleProviderContext, plugin,
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
}