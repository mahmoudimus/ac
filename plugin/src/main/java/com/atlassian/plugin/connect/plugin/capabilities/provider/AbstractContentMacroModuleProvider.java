package com.atlassian.plugin.connect.plugin.capabilities.provider;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
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
import com.atlassian.plugin.connect.plugin.integration.plugins.ConnectAddonI18nManager;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.google.common.collect.ImmutableList;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractContentMacroModuleProvider<T extends BaseContentMacroModuleBean>
        implements ConnectModuleProvider<T>
{
    private static final Logger log = LoggerFactory.getLogger(AbstractContentMacroModuleProvider.class);
    private final WebItemModuleDescriptorFactory webItemModuleDescriptorFactory;
    private final HostContainer hostContainer;
    private final AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter;
    private final ConnectAddonI18nManager connectAddonI18nManager;
    protected final IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry;
    protected final IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory;

    public AbstractContentMacroModuleProvider(WebItemModuleDescriptorFactory webItemModuleDescriptorFactory,
                                              HostContainer hostContainer, AbsoluteAddOnUrlConverter absoluteAddOnUrlConverter, IFrameRenderStrategyRegistry iFrameRenderStrategyRegistry, IFrameRenderStrategyBuilderFactory iFrameRenderStrategyBuilderFactory, ConnectAddonI18nManager connectAddonI18nManager)
    {
        this.webItemModuleDescriptorFactory = webItemModuleDescriptorFactory;
        this.hostContainer = hostContainer;
        this.absoluteAddOnUrlConverter = absoluteAddOnUrlConverter;
        this.iFrameRenderStrategyRegistry = iFrameRenderStrategyRegistry;
        this.iFrameRenderStrategyBuilderFactory = iFrameRenderStrategyBuilderFactory;
        this.connectAddonI18nManager = connectAddonI18nManager;
    }

    protected abstract ModuleDescriptor createMacroModuleDescriptor(ConnectAddonBean addon, Plugin theConnectPlugin, T macroBean);

    public List<ModuleDescriptor> provideModules(ConnectAddonBean addon, Plugin theConnectPlugin, String jsonFieldName, List<T> beans)
    {
        List<ModuleDescriptor> moduleDescriptors = newArrayList();
        MacroI18nBuilder i18nBuilder = new MacroI18nBuilder(addon.getKey());

        for (T bean : beans)
        {
            moduleDescriptors.addAll(createModuleDescriptors(addon, theConnectPlugin, bean));
            i18nBuilder.add(bean, addon);
        }

        try
        {
            connectAddonI18nManager.add(addon.getKey(), i18nBuilder.getI18nProperties());
        }
        catch (IOException e)
        {
            log.error("Unable to register I18n properties for addon: " + addon.getKey(), e);
        }

        return moduleDescriptors;
    }

    protected List<ModuleDescriptor> createModuleDescriptors(ConnectAddonBean addon, Plugin theConnectPlugin, T macroBean)
    {
        List<ModuleDescriptor> descriptors = newArrayList();

        // The actual Macro module descriptor
        descriptors.add(createMacroModuleDescriptor(addon, theConnectPlugin, macroBean));

        // Add a web item if the Macro is featured
        if (macroBean.isFeatured())
        {
            WebItemModuleBean featuredWebItem = createFeaturedWebItem(addon, macroBean);
            descriptors.add(webItemModuleDescriptorFactory.createModuleDescriptor(addon, theConnectPlugin, featuredWebItem));

            // Add a featured icon web resource
            if (macroBean.hasIcon())
            {
                descriptors.add(createFeaturedIconWebResource(addon, theConnectPlugin, macroBean));
            }
        }

        if (macroBean.hasEditor())
        {
            createEditorIFrame(addon, macroBean);
            descriptors.add(createEditorWebResource(addon, theConnectPlugin, macroBean));
        }

        return ImmutableList.copyOf(descriptors);
    }


    private WebItemModuleBean createFeaturedWebItem(ConnectAddonBean addon, T bean)
    {
        WebItemModuleBeanBuilder webItemBean = newWebItemBean()
                // due to issues with Confluence not reloading i18n properties, we have to use the raw name here
                // TODO use i18n key when we fix Confluence to support reloading i18n
                .withName(new I18nProperty(bean.getName().getValue(), bean.getName().getValue()))
                .withKey(bean.getRawKey())
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
    private ModuleDescriptor createFeaturedIconWebResource(ConnectAddonBean addon, Plugin theConnectPlugin, T bean)
    {
        String macroKey = bean.getKey(addon);

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
                .addAttribute("value", getAbsoluteUrl(addon, bean.getIcon().getUrl()));

        ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
        jsDescriptor.init(theConnectPlugin, webResource);

        return jsDescriptor;
    }

    private void createEditorIFrame(ConnectAddonBean addon, T macroBean)
    {
        MacroEditorBean editor = macroBean.getEditor();

        IFrameRenderStrategy renderStrategy = iFrameRenderStrategyBuilderFactory.builder()
                .addOn(addon.getKey())
                .module(macroBean.getKey(addon))
                .dialogTemplate()
                .urlTemplate(editor.getUrl())
                .title(macroBean.getDisplayName())
                .dimensions(editor.getWidth(), editor.getHeight())
                .simpleDialog(true)
                .build();

        iFrameRenderStrategyRegistry.register(addon.getKey(), macroBean.getRawKey(), renderStrategy);
    }

    private ModuleDescriptor createEditorWebResource(ConnectAddonBean addon, Plugin theConnectPlugin, T macroBean)
    {
        String macroKey = macroBean.getKey(addon);

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

        Element transformer = transformation
                .addElement("transformer")
                .addAttribute("key", "confluence-macroVariableTransformer")
                .addElement("var")
                .addAttribute("name", "MACRONAME")
                .addAttribute("value", macroKey).getParent()
                .addElement("var")
                .addAttribute("name", "URL")
                .addAttribute("value", ConnectIFrameServlet.iFrameServletPath(addon.getKey(), macroBean.getRawKey())).getParent()
                .addElement("var")
                .addAttribute("name", "WIDTH")
                .addAttribute("value", macroBean.getEditor().getWidth()).getParent()
                .addElement("var")
                .addAttribute("name", "HEIGHT")
                .addAttribute("value", macroBean.getEditor().getHeight()).getParent();

        createInsertTitle(macroBean, transformer);
        createEditTitle(macroBean, transformer);

        ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
        jsDescriptor.init(theConnectPlugin, webResource);

        return jsDescriptor;
    }

    private void createInsertTitle(T macroBean, Element transformer)
    {
        MacroEditorBean editor = macroBean.getEditor();
        Element insertTitleElement = transformer.addElement("var")
                .addAttribute("name", "INSERT_TITLE");

        if (editor.hasInsertTitle())
        {
            insertTitleElement.addAttribute("value", editor.getInsertTitle().getValue());
        }
        else
        {
            insertTitleElement.addAttribute("value", macroBean.getDisplayName());
            insertTitleElement.addAttribute("i18n-key", "macro.browser.insert.macro.title");
        }
    }

    private void createEditTitle(T macroBean, Element transformer)
    {
        MacroEditorBean editor = macroBean.getEditor();
        Element editTitleElement = transformer.addElement("var")
                .addAttribute("name", "EDIT_TITLE");

        if (editor.hasEditTitle())
        {
            editTitleElement.addAttribute("value", editor.getEditTitle().getValue());
        }
        else
        {
            editTitleElement.addAttribute("value", macroBean.getDisplayName());
            editTitleElement.addAttribute("i18n-key", "macro.browser.edit.macro.title");
        }
    }

    private String getAbsoluteUrl(ConnectAddonBean addon, String url)
    {
        try
        {
            return absoluteAddOnUrlConverter.getAbsoluteUrl(addon.getKey(), url);
        }
        catch (URISyntaxException e)
        {
            throw new PluginParseException("Malformed icon url declared by '"
                    + addon.getName()
                    + "' (" + addon.getKey() + "): "
                    + url, e);
        }
    }
}
