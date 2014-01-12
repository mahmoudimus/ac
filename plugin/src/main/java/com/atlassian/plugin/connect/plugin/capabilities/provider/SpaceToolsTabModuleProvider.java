package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.confluence.plugin.descriptor.web.conditions.SpaceSidebarCondition;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConfluenceConditions;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SpaceToolsActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.CompositeConditionBean.newCompositeConditionBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.SingleConditionBean.newSingleConditionBean;
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

    public static final String SPACE_ADMIN_KEY_SUFFIX = "-legacy-space-admin";

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final SpaceToolsActionDescriptorFactory spaceTabActionDescriptorFactory;
    private final ProductAccessor productAccessor;

    @Autowired
    public SpaceToolsTabModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory, SpaceToolsActionDescriptorFactory spaceTabActionDescriptorFactory, ProductAccessor productAccessor)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.spaceTabActionDescriptorFactory = spaceTabActionDescriptorFactory;
        this.productAccessor = productAccessor;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<ConnectPageModuleBean> beans)
    {
        List<ModuleDescriptor> modules = newArrayList();
        for (ConnectPageModuleBean bean : beans)
        {
            String key = bean.getKey();
            String spaceAdminLegacyKey = key + SPACE_ADMIN_KEY_SUFFIX;

            Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
            String location = isNullOrEmpty(bean.getLocation()) ? DEFAULT_LOCATION : bean.getLocation();

            String url = SpaceToolsActionDescriptorFactory.NAMESPACE_PREFIX + plugin.getKey() + "/" + key + ".action?key=${space.key}";

            List<ConditionalBean> conditions = bean.getConditions();
            if (conditions == null)
            {
                conditions = newArrayList();
            }

            WebItemModuleBean spaceToolsWebItemBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(bean.getKey())
                    .withUrl(url)
                    .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                    .withWeight(weight)
                    .withConditions(conditions)
                    .withConditions(newSingleConditionBean()
                            .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                            .build())
                    .build();

            WebItemModuleBean spaceAdminWebItemBean = newWebItemBean()
                    .withName(bean.getName())
                    .withKey(spaceAdminLegacyKey)
                    .withUrl(url)
                    .withLocation(SPACE_ADMIN_SECTION + "/spaceops")
                    .withWeight(weight)
                    .withConditions(conditions)
                    .withConditions(newSingleConditionBean()
                            .withCondition(ConfluenceConditions.SPACE_SIDEBAR)
                            .withInvert(true)
                            .build())
                    .build();

            modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, spaceToolsWebItemBean));
            modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, spaceAdminWebItemBean));
            modules.add(this.spaceTabActionDescriptorFactory.create(plugin, key, spaceAdminLegacyKey, bean.getDisplayName(), bean.getUrl()));
        }
        return modules;
    }
}
