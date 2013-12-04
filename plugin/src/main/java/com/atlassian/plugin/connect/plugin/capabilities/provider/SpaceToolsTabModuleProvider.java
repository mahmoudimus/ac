package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.Collections;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SpaceToolsActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.page.PageInfo;
import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;

import com.google.common.collect.Lists;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Confluence "Space Tools" tabs are modelled as a web-item and an x-work action.
 */
@Component
public class SpaceToolsTabModuleProvider implements ConnectModuleProvider<ConnectPageCapabilityBean>
{
    private static final String SPACE_TOOLS_SECTION = "system.space.tools";
    private static final String DEFAULT_LOCATION = "addons";

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
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<ConnectPageCapabilityBean> beans)
    {
        List<ModuleDescriptor> modules = Lists.newArrayList();
        for (ConnectPageCapabilityBean bean : beans)
        {
            String key = bean.getKey();

            Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
            String location = isNullOrEmpty(bean.getLocation()) ? DEFAULT_LOCATION : bean.getLocation();

            String url = SpaceToolsActionDescriptorFactory.NAMESPACE_PREFIX + plugin.getKey() + "/" + key + ".action?key=${space.key}";
            WebItemCapabilityBean webItemCapabilityBean = newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withLink(url)
                .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                .withWeight(weight)
                .build();

            modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemCapabilityBean));
            modules.add(this.spaceTabActionDescriptorFactory.create(plugin, key, bean.getDisplayName(), bean.getUrl()));
        }
        return modules;
    }
}
