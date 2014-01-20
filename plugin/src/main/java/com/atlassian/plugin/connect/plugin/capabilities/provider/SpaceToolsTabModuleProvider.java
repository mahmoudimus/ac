package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConfluenceConditions;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.modules.util.ModuleKeyGenerator;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.XWorkActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsContextInterceptor;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
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
public class SpaceToolsTabModuleProvider implements ConnectModuleProvider<ConnectPageModuleBean>
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
    private final UrlVariableSubstitutor urlVariableSubstitutor;

    @Autowired
    public SpaceToolsTabModuleProvider(final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            final XWorkActionDescriptorFactory xWorkActionDescriptorFactory, ProductAccessor productAccessor,
            final UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.xWorkActionDescriptorFactory = xWorkActionDescriptorFactory;
        this.productAccessor = productAccessor;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<ConnectPageModuleBean> beans)
    {
        List<ModuleDescriptor> modules = newArrayList();
        for (ConnectPageModuleBean bean : beans)
        {
            XWorkActionModuleBean actionBean = createActionBean(plugin, bean);
            modules.add(xWorkActionDescriptorFactory.create(plugin, actionBean));

            String actionUrl = actionBean.getUrl() + "?key=${space.key}";
            for (WebItemModuleBean webItemModuleBean : createWebItemBeans(bean, actionUrl))
            {
                modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemModuleBean));
            }
        }
        return modules;
    }

    private XWorkActionModuleBean createActionBean(Plugin plugin, ConnectPageModuleBean bean)
    {
        PageInfo pageInfo = new PageInfo("", "", bean.getDisplayName(), null, Collections.EMPTY_MAP);
        String spaceAdminLegacyKey = bean.getKey() + SPACE_ADMIN_KEY_SUFFIX;
        SpaceToolsTabContext spaceTabContext = new SpaceToolsTabContext(plugin, urlVariableSubstitutor, bean.getUrl(), bean.getKey(), spaceAdminLegacyKey, pageInfo);

        return newXWorkActionBean()
                .withName(bean.getName())
                .withKey(ModuleKeyGenerator.nameToKey(bean.getName().getValue()))
                .withNamespace("/plugins/atlassian-connect/" + plugin.getKey())
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

    private List<WebItemModuleBean> createWebItemBeans(ConnectPageModuleBean bean, String actionUrl)
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
                .withKey(bean.getKey())
                .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                .withConditions(newSingleConditionBean()
                        .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                        .build())
                .build();

        WebItemModuleBean spaceAdminWebItemBean = newWebItemBean(baseWebItemBean)
                .withKey(bean.getKey() + SPACE_ADMIN_KEY_SUFFIX)
                .withLocation(SPACE_ADMIN_SECTION + "/" + LEGACY_LOCATION)
                .withConditions(newSingleConditionBean()
                        .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                        .withInvert(true)
                        .build())
                .build();

        return ImmutableList.of(spaceToolsWebItemBean, spaceAdminWebItemBean);
    }
}
