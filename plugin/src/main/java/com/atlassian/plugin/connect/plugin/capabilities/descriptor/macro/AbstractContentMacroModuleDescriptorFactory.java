package com.atlassian.plugin.connect.plugin.capabilities.descriptor.macro;

import java.net.URISyntaxException;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectDocumentationBeanFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.module.ImagePlaceholderMacro;
import com.atlassian.plugin.connect.plugin.capabilities.provider.MacroI18nBuilder;
import com.atlassian.plugin.connect.plugin.module.confluence.FixedXhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.confluence.PageMacro;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.upm.spi.PluginInstallException;
import com.atlassian.uri.Uri;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

import static com.atlassian.plugin.connect.modules.beans.nested.LinkBean.newLinkBean;

public abstract class AbstractContentMacroModuleDescriptorFactory<B extends BaseContentMacroModuleBean> implements ConnectModuleDescriptorFactory<B, XhtmlMacroModuleDescriptor>
{
    private final AbsoluteAddOnUrlConverter urlConverter;

    public AbstractContentMacroModuleDescriptorFactory(
            AbsoluteAddOnUrlConverter urlConverter)
    {
        this.urlConverter = urlConverter;
    }

    protected abstract ModuleFactory createModuleFactory(ConnectAddonBean addon, DOMElement element, B bean);

    @Override
    public XhtmlMacroModuleDescriptor createModuleDescriptor(ConnectAddonBean addon, Plugin theConnectPlugin, B bean)
    {
        DOMElement element = createDOMElement(addon, bean);
        ModuleFactory moduleFactory = createModuleFactory(addon, element, bean);
        MacroMetadataParser macroMetadataParser = createMacroMetaDataParser(addon, bean);

        FixedXhtmlMacroModuleDescriptor descriptor = new FixedXhtmlMacroModuleDescriptor(moduleFactory, macroMetadataParser, addon.getKey() + ":" + bean.getRawKey());
        descriptor.init(theConnectPlugin, element);

        return descriptor;
    }

    protected DOMElement createDOMElement(ConnectAddonBean addon, B bean)
    {
        DOMElement element = new DOMElement("macro");
        // For macros, the name has to be a key and can't contain spaces etc.
        String macroName = bean.getKey(addon);
        // If 'featured' is true, the web item needs the macro name as it's key...
        // So chose a different prefix for the macro itself
        element.setAttribute("key", "macro-" + macroName);
        element.setAttribute("name", macroName);
        element.setAttribute("i18n-name-key", MacroI18nBuilder.getMacroI18nKey(addon.getKey(), macroName));
        element.setAttribute("class", PageMacro.class.getName());
        element.setAttribute("state", "enabled");

        if (bean.hasDocumentation())
        {
            element.setAttribute("documentation-url", bean.getDocumentation().getUrl());
        }
        if (bean.hasIcon())
        {
            element.setAttribute("icon", getAbsoluteUrl(addon, bean.getIcon().getUrl()));
        }

        handleParameters(bean, element);
        handleCategories(bean, element);
        handleAliases(bean, element);

        return element;
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

    private MacroMetadataParser createMacroMetaDataParser(ConnectAddonBean addon, B bean)
    {
        ConnectDocumentationBeanFactory docBeanFactory = new ConnectDocumentationBeanFactory(makeAbsolute(addon, bean.getDocumentation()));
        return new MacroMetadataParser(docBeanFactory);
    }

    protected ImagePlaceholderMacro decorateWithImagePlaceHolder(ConnectAddonBean addon, Macro macro, ImagePlaceholderBean bean)
    {
        String absoluteUrl = getAbsoluteUrl(addon, bean.getUrl());
        Dimensions dimensions = null;
        if (null != bean.getHeight() && null != bean.getWidth())
        {
            dimensions = new Dimensions(bean.getWidth(), bean.getHeight());
        }
        return new ImagePlaceholderMacro(macro, Uri.parse(absoluteUrl), dimensions, bean.applyChrome());
    }

    private LinkBean makeAbsolute(ConnectAddonBean addon, LinkBean documentation)
    {
        if (null != documentation)
        {
            String absoluteUrl = getAbsoluteUrl(addon, documentation.getUrl());
            return newLinkBean(documentation).withUrl(absoluteUrl).build();
        }
        return null;
    }

    private String getAbsoluteUrl(ConnectAddonBean addon, String url)
    {
        try
        {
            return urlConverter.getAbsoluteUrl(addon.getKey(), url);
        }
        catch (URISyntaxException e)
        {
            // help vendors find errors in their descriptors
            throw new PluginInstallException("Malformed url declared by '"
                    + addon.getName()
                    + "' (" + addon.getKey() + "): "
                    + url, e);
        }
    }
}
