package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.MacroI18nBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;

public abstract class AbstractContentMacroModuleProvider<T extends BaseContentMacroModuleBean> implements ConnectModuleProvider<T>
{
    private static final Logger log = LoggerFactory.getLogger(AbstractContentMacroModuleProvider.class);

    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final HostContainer hostContainer;
    private final AbsoluteAddOnUrlConverter urlConverter;

    public AbstractContentMacroModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory, HostContainer hostContainer, AbsoluteAddOnUrlConverter urlConverter)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.hostContainer = hostContainer;
        this.urlConverter = urlConverter;
    }

    protected abstract ModuleDescriptor createMacroModuleDescriptor(Plugin plugin, BundleContext bundleContext, T macroBean);

    public List<ModuleDescriptor> provideModules(Plugin plugin, BundleContext addonBundleContext, String jsonFieldName, List<T> beans)
    {
        List<ModuleDescriptor> moduleDescriptors = Lists.newArrayList();

        for (T bean : beans)
        {
            moduleDescriptors.addAll(createModuleDescriptors(plugin, addonBundleContext, bean));
        }

        return moduleDescriptors;
    }

    protected List<ModuleDescriptor> createModuleDescriptors(Plugin plugin, BundleContext bundleContext, T macroBean)
    {
        List<ModuleDescriptor> descriptors = Lists.newArrayList();

        // The actual Macro module descriptor
        descriptors.add(createMacroModuleDescriptor(plugin, bundleContext, macroBean));

        // Add a web item if the Macro is featured
        if (macroBean.isFeatured())
        {
            WebItemModuleBean featuredWebItem = createFeaturedWebItem(plugin, macroBean);
            descriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, bundleContext, featuredWebItem));

            // Add a featured icon web resource
            if (null != macroBean.getIcon())
            {
                descriptors.add(createFeaturedIconWebResource(plugin, macroBean));
            }
        }

        // TODO: Add Image Placeholder --> ACDEV-678
        // TODO: Add Editor --> ACDEV-676

        return ImmutableList.copyOf(descriptors);
    }

    // No web-resource beans/builders/descriptors/providers, so falling back to XML
    public ModuleDescriptor createFeaturedIconWebResource(Plugin plugin, T bean)
    {
        String macroKey = bean.getKey();

        Element webResource = new DOMElement("web-resource")
                .addAttribute("key", macroKey + "-featured-macro-resources");

        webResource.addElement("resource")
                .addAttribute("type", "download")
                .addAttribute("name", macroKey + "-icon.css")
                .addAttribute("location", "css/confluence/macro/featured-macro-icon.css");

        webResource.addElement("context")
                .setText("editor");

        Element transformation = webResource.addElement("transformation")
                .addAttribute("extension", "css");

        transformation.addElement("transformer")
                .addAttribute("key", "confluence-macroVariableTransformer")
                .addElement("var")
                .addAttribute("name", "KEY")
                .addAttribute("value", macroKey).getParent()
                .addElement("var")
                .addAttribute("name", "ICON_URL")
                .addAttribute("value", getAbsoluteUrl(plugin, bean.getIcon().getUrl()));

        ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
        jsDescriptor.init(plugin, webResource);

        return jsDescriptor;
    }

    private WebItemModuleBean createFeaturedWebItem(Plugin plugin, T bean)
    {
        WebItemModuleBeanBuilder webItemBean = newWebItemBean()
                .withName(new I18nProperty(bean.getName().getValue(),
                        MacroI18nBuilder.getMacroI18nKey(plugin.getKey(), bean.getKey())))
                .withKey(bean.getKey())
                .withLocation("system.editor.featured.macros.default");

        if (null != bean.getIcon())
        {
            webItemBean.withIcon(IconBean.newIconBean()
                    .withUrl(bean.getIcon().getUrl())
                    .withWidth(16)
                    .withHeight(16)
                    .build());
        }
        return webItemBean.build();
    }

    private String getAbsoluteUrl(Plugin plugin, String url)
    {
        try
        {
            return urlConverter.getAbsoluteUrl(plugin.getKey(), url);
        }
        catch (URISyntaxException e)
        {
            throw new PluginParseException("Malformed icon url declared by '"
                    + plugin.getName()
                    + "' (" + plugin.getKey() + "): "
                    + url, e);
        }
    }
}
