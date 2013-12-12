package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.DynamicContentMacroModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;

@ConfluenceComponent
public class DynamicContentMacroModuleProvider implements ConnectModuleProvider<DynamicContentMacroModuleBean>
{
    private final DynamicContentMacroModuleDescriptorFactory dynamicContentMacroModuleDescriptorFactory;
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;

    @Autowired
    public DynamicContentMacroModuleProvider(DynamicContentMacroModuleDescriptorFactory macroModuleDescriptorFactory,
                                             WebItemModuleDescriptorFactory webItemModuleDescriptorFactory)
    {
        this.dynamicContentMacroModuleDescriptorFactory = macroModuleDescriptorFactory;
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
    }

    @Override
    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<DynamicContentMacroModuleBean> beans)
    {
        List<ModuleDescriptor> moduleDescriptors = Lists.newArrayList();

        for (DynamicContentMacroModuleBean bean : beans)
        {
            moduleDescriptors.addAll(createModuleDescriptors(plugin, addonBundleContext, bean));
        }

        return moduleDescriptors;
    }

    private List<ModuleDescriptor> createModuleDescriptors(Plugin plugin, BundleContext bundleContext, DynamicContentMacroModuleBean macroBean)
    {
        List<ModuleDescriptor> descriptors = Lists.newArrayList();

        // The actual Macro module descriptor
        descriptors.add(dynamicContentMacroModuleDescriptorFactory.createModuleDescriptor(plugin, bundleContext, macroBean));

        // Add a web item if the Macro is featured
        if (macroBean.getFeatured())
        {
            WebItemModuleBean featuredWebItem = createFeaturedWebItem(macroBean);
            descriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, bundleContext, featuredWebItem));

            // Add a featured icon web resource
            if (macroBean.getIcon().hasUrl())
            {
                descriptors.add(createFeaturedIconWebResource(plugin, macroBean));
            }
        }

        // TODO: Add Image Placeholder --> ACDEV-678
        // TODO: Add Editor --> ACDEV-676

        return ImmutableList.copyOf(descriptors);
    }

    private ModuleDescriptor createFeaturedIconWebResource(Plugin plugin, DynamicContentMacroModuleBean bean)
    {
        return dynamicContentMacroModuleDescriptorFactory.createFeaturedIconWebResource(plugin, bean);
    }

    private WebItemModuleBean createFeaturedWebItem(DynamicContentMacroModuleBean bean)
    {
        return newWebItemBean()
            .withName(bean.getName())
            .withKey("editor-featured-macro-" + bean.getKey())
            .withLocation("system.editor.featured.macros.default")
            .build();
    }
}
