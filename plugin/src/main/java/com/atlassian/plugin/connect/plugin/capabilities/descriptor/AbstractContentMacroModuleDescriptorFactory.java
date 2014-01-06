package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.macro.DefaultImagePlaceholder;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.ImagePlaceholderMacro;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.plugin.module.confluence.FixedXhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.confluence.PageMacro;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.upm.spi.PluginInstallException;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;

import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean.newLinkBean;

public abstract class AbstractContentMacroModuleDescriptorFactory<B extends BaseContentMacroModuleBean> implements ConnectModuleDescriptorFactory<B, XhtmlMacroModuleDescriptor>
{
    private final AbsoluteAddOnUrlConverter urlConverter;
    private final I18nPropertiesPluginManager i18nPropertiesPluginManager;

    public AbstractContentMacroModuleDescriptorFactory(
            AbsoluteAddOnUrlConverter urlConverter,
            I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        this.urlConverter = urlConverter;
        this.i18nPropertiesPluginManager = i18nPropertiesPluginManager;
    }

    protected abstract ModuleFactory createModuleFactory(Plugin plugin, DOMElement element, B bean);

    @Override
    public XhtmlMacroModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, B bean)
    {
        DOMElement element = createDOMElement(plugin, bean);
        ModuleFactory moduleFactory = createModuleFactory(plugin, element, bean);
        MacroMetadataParser macroMetadataParser = createMacroMetaDataParser(plugin, bean);

        FixedXhtmlMacroModuleDescriptor descriptor = new FixedXhtmlMacroModuleDescriptor(moduleFactory, macroMetadataParser);
        descriptor.init(plugin, element);

        registerI18nProperties(plugin, bean);

        return descriptor;
    }

    protected DOMElement createDOMElement(Plugin plugin, B bean)
    {
        DOMElement element = new DOMElement("macro");
        // If 'featured' is true, the web item needs the macro name as it's key...
        // So chose a different prefix for the macro itself
        element.setAttribute("key", "macro-" + bean.getKey());
        // For macros, the name has to be a key and can't contain spaces etc.
        element.setAttribute("name", bean.getKey());
        element.setAttribute("i18n-name-key", MacroI18nBuilder.getMacroI18nKey(plugin.getKey(), bean.getKey()));
        element.setAttribute("class", PageMacro.class.getName());
        element.setAttribute("state", "enabled");

        if (bean.hasDocumentation())
        {
            element.setAttribute("documentation-url", bean.getDocumentation().getUrl());
        }
        if (bean.hasIcon())
        {
            element.setAttribute("icon", getAbsoluteUrl(plugin, bean.getIcon().getUrl()));
        }

        handleParameters(bean, element);
        handleCategories(bean, element);
        handleAliases(bean, element);

        return element;
    }

    protected void registerI18nProperties(Plugin plugin, B bean)
    {
        MacroI18nBuilder i18nBuilder = new MacroI18nBuilder(plugin.getKey(), bean.getKey());

        i18nBuilder.addName(bean.getName());
        i18nBuilder.addDescription(bean.getDescription());

        for (MacroParameterBean parameterBean : bean.getParameters())
        {
            i18nBuilder.addParameterLabel(parameterBean.getIdentifier(), parameterBean.getName());
            i18nBuilder.addParameterDescription(parameterBean.getIdentifier(), parameterBean.getDescription());
        }
        i18nPropertiesPluginManager.add(plugin.getKey(), i18nBuilder.getI18nProperties());
    }

    private void handleAliases(B bean, DOMElement element)
    {
        for (String alias : bean.getAliases())
        {
            element.addElement("alias").addAttribute("name", alias);
        }
    }

    private void handleCategories(B bean, DOMElement element)
    {
        for (String category : bean.getCategories())
        {
            element.addElement("category").addAttribute("name", category);
        }
    }

    private void handleParameters(B bean, DOMElement element)
    {
        Element parameters = element.addElement("parameters");
        for (MacroParameterBean parameterBean : bean.getParameters())
        {
            Element parameter = parameters.addElement("parameter")
                    .addAttribute("name", parameterBean.getIdentifier())
                    .addAttribute("type", parameterBean.getType());

            if (parameterBean.isRequired())
            {
                parameter.addAttribute("required", "true");
            }
            if (parameterBean.isMultiple())
            {
                parameter.addAttribute("multiple", "true");
            }
            if (parameterBean.hasDefaultValue())
            {
                parameter.addAttribute("default", parameterBean.getDefaultValue());
            }
            for (String value : parameterBean.getValues())
            {
                parameter.addElement("value").addAttribute("name", value);
            }
            for (String value : parameterBean.getAliases())
            {
                parameter.addElement("alias").addAttribute("name", value);
            }
        }
    }

    protected MacroMetadataParser createMacroMetaDataParser(Plugin plugin, B bean)
    {
        ConnectDocumentationBeanFactory docBeanFactory = new ConnectDocumentationBeanFactory(makeAbsolute(plugin, bean.getDocumentation()));
        return new MacroMetadataParser(docBeanFactory);
    }

    protected ImagePlaceholderMacro decorateWithImagePlaceHolder(Plugin plugin, Macro macro, ImagePlaceholderBean bean)
    {
        String absoluteUrl = getAbsoluteUrl(plugin, bean.getUrl());
        Dimensions dimensions = null;
        if (null != bean.getHeight() && null != bean.getWidth())
        {
            dimensions = new Dimensions(bean.getWidth(), bean.getHeight());
        }
        boolean applyChrome = bean.applyChrome();

        return new ImagePlaceholderMacro(macro, new DefaultImagePlaceholder(absoluteUrl, dimensions, applyChrome));
    }

    private LinkBean makeAbsolute(Plugin plugin, LinkBean documentation)
    {
        if (null != documentation)
        {
            String absoluteUrl = getAbsoluteUrl(plugin, documentation.getUrl());
            return newLinkBean(documentation).withUrl(absoluteUrl).build();
        }
        return null;
    }

    private String getAbsoluteUrl(Plugin plugin, String url)
    {
        try
        {
            return urlConverter.getAbsoluteUrl(plugin.getKey(), url);
        }
        catch (URISyntaxException e)
        {
            // help vendors find errors in their descriptors
            throw new PluginInstallException("Malformed url declared by '"
                    + plugin.getName()
                    + "' (" + plugin.getKey() + "): "
                    + url, e);
        }
    }
}
