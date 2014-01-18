package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConfluenceConditions;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.XWorkActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsContextInterceptor;
import com.atlassian.plugin.connect.plugin.module.confluence.SpaceToolsIFrameAction;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.page.SpaceToolsTabContext;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.XWorkActionModuleBean.newXWorkActionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static com.atlassian.plugin.connect.modules.beans.nested.XWorkInterceptorBean.newXWorkInterceptorBean;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Confluence "Space Tools" tabs are modelled as a web-item and an x-work action.
 */
@Component
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
            for (WebItemModuleBean webItemModuleBean : createWebItemBeans(plugin, addonBundleContext, bean, actionUrl))
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

    private List<WebItemModuleBean> createWebItemBeans(Plugin plugin, BundleContext addonBundleContext,
            ConnectPageModuleBean bean, String actionUrl)
    {
        Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
        String location = isNullOrEmpty(bean.getLocation()) ? DEFAULT_LOCATION : bean.getLocation();

        List<ConditionalBean> conditions = bean.getConditions();
        if (conditions == null)
        {
            conditions = newArrayList();
        }

        WebItemModuleBean spaceToolsWebItemBean = newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withUrl(actionUrl)
                .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                .withWeight(weight)
                .withConditions(conditions)
                .withConditions(newSingleConditionBean()
                        .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                        .build())
                .build();

        String spaceAdminLegacyKey = bean.getKey() + SPACE_ADMIN_KEY_SUFFIX;
        WebItemModuleBean spaceAdminWebItemBean = newWebItemBean()
                .withName(bean.getName())
                .withKey(spaceAdminLegacyKey)
                .withUrl(actionUrl)
                .withLocation(SPACE_ADMIN_SECTION + "/" + LEGACY_LOCATION)
                .withWeight(weight)
                .withConditions(conditions)
                .withConditions(newSingleConditionBean()
                        .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                        .withInvert(true)
                        .build())
                .build();

        return ImmutableList.of(spaceToolsWebItemBean, spaceAdminWebItemBean);
    }
}
