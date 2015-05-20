package com.atlassian.plugin.connect.confluence.capabilities.descriptor.macro;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.browser.beans.MacroParameter;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringEscapeUtils;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.confluence.capabilities.descriptor.ConnectDocumentationBeanFactory;
import com.atlassian.plugin.connect.confluence.macro.ImagePlaceholderMacro;
import com.atlassian.plugin.connect.confluence.macro.PageMacro;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ImagePlaceholderBean;
import com.atlassian.plugin.connect.modules.beans.nested.LinkBean;
import com.atlassian.plugin.connect.modules.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectModuleDescriptorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleProviderContext;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.uri.Uri;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.nested.LinkBean.newLinkBean;
import static com.atlassian.plugin.connect.spi.util.Dom4jUtils.printNode;

public abstract class AbstractContentMacroModuleDescriptorFactory<B extends BaseContentMacroModuleBean>
        implements ConnectModuleDescriptorFactory<B, XhtmlMacroModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(AbstractContentMacroModuleDescriptorFactory.class);

    private final AbsoluteAddOnUrlConverter urlConverter;

    public AbstractContentMacroModuleDescriptorFactory(
            AbsoluteAddOnUrlConverter urlConverter)
    {
        this.urlConverter = urlConverter;
    }

    protected abstract ModuleFactory createModuleFactory(ConnectAddonBean addon, DOMElement element, B bean);

    @Override
    public XhtmlMacroModuleDescriptor createModuleDescriptor(ConnectModuleProviderContext moduleProviderContext, Plugin theConnectPlugin, B bean)
    {
        final ConnectAddonBean connectAddonBean = moduleProviderContext.getConnectAddonBean();
        DOMElement element = createDOMElement(connectAddonBean, bean);
        ModuleFactory moduleFactory = createModuleFactory(connectAddonBean, element, bean);
        MacroMetadataParser macroMetadataParser = createMacroMetaDataParser(connectAddonBean, bean);

        FixedXhtmlMacroModuleDescriptor descriptor = new FixedXhtmlMacroModuleDescriptor(moduleFactory, macroMetadataParser);
        descriptor.init(theConnectPlugin, element);

        // TODO: Remove once we have proper i18n support
        updateDefaultParameterLabels(descriptor.getMacroMetadata().getFormDetails().getParameters(), bean.getParameters());

        return descriptor;
    }

    private void updateDefaultParameterLabels(List<MacroParameter> macroParameters, List<MacroParameterBean> macroParameterBeans)
    {
        Map<String, MacroParameter> parameterMap = Maps.uniqueIndex(macroParameters, new Function<MacroParameter, String>()
        {
            @Override
            public String apply(MacroParameter parameter)
            {
                Preconditions.checkNotNull(parameter, "Implementation error: parameter must never be null");
                return parameter.getName();
            }
        });
        for (MacroParameterBean parameterBean : macroParameterBeans)
        {
            MacroParameter macroParameter = parameterMap.get(parameterBean.getIdentifier());
            Preconditions.checkNotNull(macroParameter, "Implementation error: Mismatch between parameters in the " +
                    "Confluence module descriptor and declared parameters in the descriptor.");
            if (parameterBean.hasName())
            {
                macroParameter.setDisplayName(parameterBean.getName().getValue());
            }
            if (parameterBean.hasDescription())
            {
                macroParameter.setDescription(parameterBean.getDescription().getValue());
            }
        }
    }

    protected DOMElement createDOMElement(ConnectAddonBean addon, B bean)
    {
        DOMElement element = new DOMElement("macro");
        // For macros, the name has to be a key and can't contain spaces etc.
        // If 'featured' is true, the web item needs the macro name as it's key...
        // So chose a different prefix for the macro itself
        element.setAttribute("key", "macro-" + bean.getRawKey());
        element.setAttribute("name", bean.getRawKey());
        // due to issues with Confluence not reloading i18n properties, we have to use the raw name here
        // TODO use i18n when we fix Confluence to support reloading i18n
        element.setAttribute("i18n-name-key", bean.getName().getValue());
        element.setAttribute("class", PageMacro.class.getName());
        element.setAttribute("state", "enabled");

        if (bean.getDescription() != null)
        {
            element.addElement("description").addCDATA(StringEscapeUtils.escapeHtml(bean.getDescription().getValue()));
        }
        if (bean.hasDocumentation())
        {
            element.setAttribute("documentation-url", bean.getDocumentation().getUrl());
        }
        if (bean.hasIcon())
        {
            element.setAttribute("icon", urlConverter.getAbsoluteUrl(addon, bean.getIcon().getUrl()));
        }
        if (bean.isHidden())
        {
            element.setAttribute("hidden", "true");
        }

        handleParameters(bean, element);
        handleCategories(bean, element);
        handleAliases(bean, element);

        if (log.isDebugEnabled())
        {
            log.debug("Created macro: " + printNode(element));
        }
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
        String absoluteUrl = urlConverter.getAbsoluteUrl(addon, bean.getUrl());
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
            String absoluteUrl = urlConverter.getAbsoluteUrl(addon, documentation.getUrl());
            return newLinkBean(documentation).withUrl(absoluteUrl).build();
        }
        return null;
    }
}
