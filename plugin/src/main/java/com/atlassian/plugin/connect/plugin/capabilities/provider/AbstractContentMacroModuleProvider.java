package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.WebItemModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.plugin.ConnectPluginInfo;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.WebItemModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategy;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyBuilderFactory;
import com.atlassian.plugin.connect.plugin.iframe.render.strategy.IFrameRenderStrategyRegistry;
import com.atlassian.plugin.connect.plugin.iframe.servlet.ConnectIFrameServlet;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

import java.net.URISyntaxException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractContentMacroModuleProvider<T extends BaseContentMacroModuleBean>
        implements ConnectModuleProvider<T>
{
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final HostContainer hostContainer;
    private final AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter;
    private final I18nPropertiesPluginManager i18nPropertiesPluginManager;
    protected final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    protected final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    public AbstractContentMacroModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
            HostContainer hostContainer, AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory, I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.hostContainer = hostContainer;
        this.absoluteAddOnUrlConverter = absoluteAddOnUrlConverter;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.i18nPropertiesPluginManager = i18nPropertiesPluginManager;
    }

    protected abstract ModuleDescriptor createMacroModuleDescriptor(Plugin plugin, T macroBean);

    public List<ModuleDescriptor> provideModules(Plugin plugin, String jsonFieldName, List<T> beans)
    {
        List<ModuleDescriptor> moduleDescriptors = newArrayList();
        MacroI18nBuilder i18nBuilder = new MacroI18nBuilder(plugin.getKey());

        for (T bean : beans)
        {
            moduleDescriptors.addAll(createModuleDescriptors(plugin, bean));
            i18nBuilder.add(bean);
        }

        i18nPropertiesPluginManager.add(plugin.getKey(), i18nBuilder.getI18nProperties());

        return moduleDescriptors;
    }

    protected List<ModuleDescriptor> createModuleDescriptors(Plugin plugin, T macroBean)
    {
        List<ModuleDescriptor> descriptors = newArrayList();

        // The actual Macro module descriptor
        descriptors.add(createMacroModuleDescriptor(plugin, macroBean));

        // Add a web item if the Macro is featured
        if (macroBean.isFeatured())
        {
            WebItemModuleBean featuredWebItem = createFeaturedWebItem(plugin, macroBean);
            descriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(plugin, featuredWebItem));

            // Add a featured icon web resource
            if (macroBean.hasIcon())
            {
                descriptors.add(createFeaturedIconWebResource(plugin, macroBean));
            }
        }

        if (macroBean.hasEditor())
        {
            createEditorIFrame(plugin, macroBean);
            descriptors.add(createEditorWebResource(plugin, macroBean));
        }

        return ImmutableList.copyOf(descriptors);
    }


    private WebItemModuleBean createFeaturedWebItem(Plugin plugin, T bean)
    {
        WebItemModuleBeanBuilder webItemBean = newWebItemBean()
                .withName(new I18nProperty(bean.getName().getValue(),
                        MacroI18nBuilder.getMacroI18nKey(plugin.getKey(), bean.getKey())))
                .withKey(bean.getKey())
                .withLocation("system.editor.featured.macros.default");

        if (bean.hasIcon())
        {
            webItemBean.withIcon(IconBean.newIconBean()
                    .withUrl(bean.getIcon().getUrl())
                    .withWidth(16)
                    .withHeight(16)
                    .build());
        }
        return webItemBean.build();
    }

    // No web-resource beans/builders/descriptors/providers, so falling back to XML
    private ModuleDescriptor createFeaturedIconWebResource(Plugin plugin, T bean)
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

    private void createEditorIFrame(Plugin plugin, T macroBean)
    {
        MacroEditorBean editor = macroBean.getEditor();

        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(plugin.getKey())
                .module(macroBean.getKey())
                .dialogTemplate()
                .urlTemplate(editor.getUrl())
                .title(macroBean.getDisplayName())
                .dimensions(editor.getWidth(), editor.getHeight())
                .simpleDialog(true)
                .build();

        iFrameRenderStrategyRegistry.register(plugin.getKey(), macroBean.getKey(), ConnectIFrameServlet.RAW_CLASSIFIER, renderStrategy);
    }

    private ModuleDescriptor createEditorWebResource(Plugin plugin, T macroBean)
    {
        MacroEditorBean editor = macroBean.getEditor();

        String macroKey = macroBean.getKey();
        String editTitle = editor.hasEditTitle() ? editor.getEditTitle().getValue() : macroBean.getDisplayName();
        String insertTitle = editor.hasInsertTitle() ? editor.getInsertTitle().getValue() : macroBean.getDisplayName();

        Element webResource = new DOMElement("web-resource")
                .addElement("web-resource")
                .addAttribute("key", macroKey + "-macro-editor-resources");

        webResource.addElement("resource")
                .addAttribute("type", "download")
                .addAttribute("name", "override.js")
                .addAttribute("location", "js/confluence/macro/override.js");

        webResource.addElement("dependency")
                .setText(ConnectPluginInfo.getPluginKey() + ":ap-amd");

        webResource.addElement("context")
                .setText("editor");

        Element transformation = webResource.addElement("transformation")
                .addAttribute("extension", "js");

        transformation
                .addElement("transformer")
                .addAttribute("key", "confluence-macroVariableTransformer")
                .addElement("var")
                .addAttribute("name", "MACRONAME")
                .addAttribute("value", macroKey).getParent()
                .addElement("var")
                .addAttribute("name", "URL")
                .addAttribute("value", ConnectIFrameServlet.iFrameServletPath(plugin.getKey(), macroBean.getKey())).getParent()
                .addElement("var")
                .addAttribute("name", "WIDTH")
                .addAttribute("value", editor.getWidth()).getParent()
                .addElement("var")
                .addAttribute("name", "HEIGHT")
                .addAttribute("value", editor.getHeight()).getParent()
                .addElement("var")
                .addAttribute("name", "EDIT_TITLE")
                .addAttribute("value", editTitle)
                .addAttribute("i18n-key", "macro.browser.edit.macro.title").getParent()
                .addElement("var")
                .addAttribute("name", "INSERT_TITLE")
                .addAttribute("value", insertTitle)
                .addAttribute("i18n-key", "macro.browser.insert.macro.title").getParent();

        ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
        jsDescriptor.init(plugin, webResource);

        return jsDescriptor;
    }

    private String getAbsoluteUrl(Plugin plugin, String url)
    {
        try
        {
            return absoluteAddOnUrlConverter.getAbsoluteUrl(plugin.getKey(), url);
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
