package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
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
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Confluence "Space Tools" tabs are modelled as a web-item and an x-work action.
 */
@Component
public class SpaceToolsTabModuleProvider implements ConnectModuleProvider<ConnectPageModuleBean>
{
    @VisibleForTesting
    public static final String SPACE_TOOLS_SECTION = "system.space.tools";
    @VisibleForTesting
    public static final String DEFAULT_LOCATION = "addons";

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
        List<ModuleDescriptor> modules = Lists.newArrayList();
        for (ConnectPageModuleBean bean : beans)
        {
            String key = bean.getKey();

            Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
            String location = isNullOrEmpty(bean.getLocation()) ? DEFAULT_LOCATION : bean.getLocation();

            String url = SpaceToolsActionDescriptorFactory.NAMESPACE_PREFIX + plugin.getKey() + "/" + key + ".action?key=${space.key}";
            WebItemModuleBean webItemModuleBean = newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withUrl(url)
                .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                .withWeight(weight)
                .build();

            modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemModuleBean));
            modules.add(this.spaceTabActionDescriptorFactory.create(plugin, key, bean.getDisplayName(), bean.getUrl()));
        }
        return modules;
    }
}
