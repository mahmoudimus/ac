package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.AbsoluteAddOnUrlConverter;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.integration.plugins.I18nPropertiesPluginManager;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.confluence.FixedXhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.confluence.PageMacro;
import com.atlassian.plugin.connect.plugin.module.confluence.RemoteMacroInfo;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.plugin.connect.spi.RemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.Sets;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean.newLinkBean;

@ConfluenceComponent
public class DynamicContentMacroModuleDescriptorFactory implements ConnectModuleDescriptorFactory<DynamicContentMacroModuleBean, XhtmlMacroModuleDescriptor>
{
    private static final Logger log = LoggerFactory.getLogger(DynamicContentMacroModuleDescriptorFactory.class);

    private final RemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final IFrameRenderer iFrameRenderer;
    private final UserManager userManager;
    private final HostContainer hostContainer;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final AbsoluteAddOnUrlConverter urlConverter;
    private final I18nPropertiesPluginManager i18nPropertiesPluginManager;


    @Autowired
    public DynamicContentMacroModuleDescriptorFactory(
            RemotablePluginAccessorFactory remotablePluginAccessorFactory,
            IFrameRenderer iFrameRenderer,
            UserManager userManager,
            HostContainer hostContainer,
            UrlVariableSubstitutor urlVariableSubstitutor,
            AbsoluteAddOnUrlConverter urlConverter,
            I18nPropertiesPluginManager i18nPropertiesPluginManager)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
        this.hostContainer = hostContainer;
        this.urlVariableSubstitutor = urlVariableSubstitutor;
        this.urlConverter = urlConverter;
        this.i18nPropertiesPluginManager = i18nPropertiesPluginManager;
    }

    @Override
    public XhtmlMacroModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, DynamicContentMacroModuleBean bean)
    {
        DOMElement element = createDOMElement(plugin, bean);
        ModuleFactory moduleFactory = createModuleFactory(plugin, element, bean);

        ConnectDocumentationBeanFactory docBeanFactory = new ConnectDocumentationBeanFactory(makeAbsolute(plugin, bean.getDocumentation()));
        MacroMetadataParser macroMetadataParser = new MacroMetadataParser(docBeanFactory);

        FixedXhtmlMacroModuleDescriptor descriptor = new FixedXhtmlMacroModuleDescriptor(moduleFactory, macroMetadataParser);
        descriptor.init(plugin, element);

        registerI18nProperties(plugin, bean);

        return descriptor;
    }

    private void registerI18nProperties(Plugin plugin, DynamicContentMacroModuleBean bean)
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

    private DOMElement createDOMElement(Plugin plugin, DynamicContentMacroModuleBean bean)
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

        if (null != bean.getWidth())
        {
            element.setAttribute("width", bean.getWidth().toString());
        }
        if (null != bean.getHeight())
        {
            element.setAttribute("height", bean.getHeight().toString());
        }
        if (bean.getDocumentation().hasUrl())
        {
            element.setAttribute("documentation-url", bean.getDocumentation().getUrl());
        }
        if (bean.getIcon().hasUrl())
        {
            element.setAttribute("icon", getAbsoluteUrl(plugin, bean.getIcon().getUrl()));
        }

        handleParameters(bean, element);
        handleCategories(bean, element);
        handleAliases(bean, element);

        return element;
    }

    private void handleAliases(DynamicContentMacroModuleBean bean, DOMElement element)
    {
        for (String alias : bean.getAliases())
        {
            element.addElement("alias").addAttribute("name", alias);
        }
    }

    private void handleCategories(DynamicContentMacroModuleBean bean, DOMElement element)
    {
        for (String category : bean.getCategories())
        {
            element.addElement("category").addAttribute("name", category);
        }
    }

    private void handleParameters(DynamicContentMacroModuleBean bean, DOMElement element)
    {
        Element parameters = element.addElement("parameters");
        for (MacroParameterBean parameterBean : bean.getParameters())
        {
            Element parameter = parameters.addElement("parameter")
                    .addAttribute("name", parameterBean.getIdentifier())
                    .addAttribute("type", parameterBean.getType().toString());

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

    // No web-resource beans/builders/descriptors/providers, so falling back to XML
    public ModuleDescriptor createFeaturedIconWebResource(Plugin plugin, DynamicContentMacroModuleBean bean)
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

    private ModuleFactory createModuleFactory(final Plugin plugin, final DOMElement element, final DynamicContentMacroModuleBean bean)
    {
        return new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                try
                {
                    // TODO: Replace context params by URL variable substitution --> ACDEV-677
                    RequestContextParameterFactory requestContextParameterFactory = new RequestContextParameterFactory(Sets.<String>newHashSet(), Sets.<String>newHashSet());
                    RemoteMacroInfo macroInfo = new RemoteMacroInfo(element, plugin.getKey(),
                            MacroEnumMapper.map(bean.getBodyType()),
                            MacroEnumMapper.map(bean.getOutputType()),
                            requestContextParameterFactory,
                            bean.createUri(), HttpMethod.GET);

                    IFrameParams params = new IFrameParamsImpl(element);
                    IFrameContext iFrameContext = new IFrameContextImpl(
                            plugin.getKey(),
                            macroInfo.getUrl(),
                            bean.getKey(),
                            params
                    );
                    return (T) new PageMacro(macroInfo, userManager, iFrameRenderer, iFrameContext, remotablePluginAccessorFactory);
                }
                catch (URISyntaxException e)
                {
                    throw new PluginParseException(e);
                }
            }
        };
    }

    private LinkBean makeAbsolute(Plugin plugin, LinkBean documentation)
    {
        if (documentation.hasUrl())
        {
            String absoluteUrl = getAbsoluteUrl(plugin, documentation.getUrl());
            return newLinkBean(documentation).withUrl(absoluteUrl).build();
        }
        return documentation;
    }

    private String getAbsoluteUrl(Plugin plugin, String url)
    {
        try
        {
            return urlConverter.getAbsoluteUrl(plugin.getKey(), url);
        }
        catch (URISyntaxException e)
        {
            logUriSyntaxError(plugin, url);
            return url;
        }
    }

    private void logUriSyntaxError(Plugin plugin, String url)
    {
        // help vendors find errors in their descriptors
        log.error("Malformed documentation link declared by '"
                + plugin.getName()
                + "' (" + plugin.getKey() + "): "
                + url);
    }
}
