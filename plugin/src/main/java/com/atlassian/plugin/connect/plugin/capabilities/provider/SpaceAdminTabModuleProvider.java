package com.atlassian.plugin.connect.plugin.capabilities.provider;

import java.util.Collections;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectPageCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.SpaceAdminTabActionDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.module.page.SpaceAdminTabContext;
import com.atlassian.plugin.connect.spi.product.ProductAccessor;

import com.google.common.collect.Lists;

import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemCapabilityBean.newWebItemBean;
import static com.google.common.base.Strings.isNullOrEmpty;

@Component
public class SpaceAdminTabModuleProvider implements ConnectModuleProvider<ConnectPageCapabilityBean>
{
    private static final String SPACE_TOOLS_SECTION = "system.space.tools";

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final SpaceAdminTabActionDescriptorFactory spaceTabActionDescriptorFactory;
    private final ProductAccessor productAccessor;

    @Autowired
    public SpaceAdminTabModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory, SpaceAdminTabActionDescriptorFactory spaceTabActionDescriptorFactory, ProductAccessor productAccessor)
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
            SpaceAdminTabContext spaceTabContext = new SpaceAdminTabContext(plugin, bean.getUrl(), bean.getKey(), "atl.general", "",
                bean.getDisplayName(), null, Collections.EMPTY_MAP);

            Integer weight = bean.getWeight() == null ? productAccessor.getPreferredGeneralWeight() : bean.getWeight();
            String location = isNullOrEmpty(bean.getLocation()) ? "addons" : bean.getLocation();

            WebItemCapabilityBean webItemCapabilityBean = newWebItemBean()
                .withName(bean.getName())
                .withKey(bean.getKey())
                .withLink("/plugins/ac/" + plugin.getKey() + "/" + bean.getKey() + ".action?key=${space.key}")
                .withLocation(SPACE_TOOLS_SECTION + "/" + location)
                .withWeight(weight)
                .build();

            modules.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, addonBundleContext, webItemCapabilityBean));
            modules.add(this.spaceTabActionDescriptorFactory.create(plugin, spaceTabContext));
        }
        return modules;
    }
}
