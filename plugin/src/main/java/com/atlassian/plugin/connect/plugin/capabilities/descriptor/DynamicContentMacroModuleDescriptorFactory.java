package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroCategory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.atlassian.plugin.connect.plugin.capabilities.util.MacroEnumMapper;
import com.atlassian.plugin.connect.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.connect.plugin.module.confluence.FixedXhtmlMacroModuleDescriptor;
import com.atlassian.plugin.connect.plugin.module.confluence.PageMacro;
import com.atlassian.plugin.connect.plugin.module.confluence.RemoteMacroInfo;
import com.atlassian.plugin.connect.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import com.atlassian.plugin.connect.plugin.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.plugin.connect.spi.http.HttpMethod;
import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;
import com.atlassian.plugin.connect.spi.module.IFrameRenderer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.Sets;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URISyntaxException;

@ConfluenceComponent
public class DynamicContentMacroModuleDescriptorFactory implements ConnectModuleDescriptorFactory<DynamicContentMacroModuleBean, XhtmlMacroModuleDescriptor>
{
    private final UserManager userManager;
    private final IFrameRenderer iFrameRenderer;
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final UrlVariableSubstitutor urlVariableSubstitutor;
    private final MacroMetadataParser macroMetadataParser;


    @Autowired
    public DynamicContentMacroModuleDescriptorFactory(
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            IFrameRenderer iFrameRenderer,
            UserManager userManager,
            UrlVariableSubstitutor urlVariableSubstitutor)
    {
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
        this.urlVariableSubstitutor = urlVariableSubstitutor;

        // todo: fix this in confluence
        this.macroMetadataParser = ComponentLocator.getComponent(MacroMetadataParser.class);
    }

    @Override
    public XhtmlMacroModuleDescriptor createModuleDescriptor(Plugin plugin, BundleContext addonBundleContext, DynamicContentMacroModuleBean bean)
    {
        DOMElement element = createDOMElement(bean);
        ModuleFactory moduleFactory = createModuleFactory(plugin, element, bean);

        FixedXhtmlMacroModuleDescriptor descriptor = new FixedXhtmlMacroModuleDescriptor(moduleFactory, macroMetadataParser);
        descriptor.init(plugin, element);
        return descriptor;
    }

    private DOMElement createDOMElement(DynamicContentMacroModuleBean bean)
    {
        DOMElement element = new DOMElement("macro");
        element.setAttribute("key", bean.getKey());
        element.setAttribute("name", bean.getDisplayName());
        element.setAttribute("i18n-name-key", bean.getName().getI18n());
        element.setAttribute("class", PageMacro.class.getName());
        element.setAttribute("state", "enabled");

        element.addElement("description")
                .addText(bean.getDescription().getValue())
                .addAttribute("key", bean.getDescription().getI18n());

        if (bean.getIcon().hasUrl())
        {
            element.setAttribute("icon", bean.getIcon().getUrl());
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
            element.addElement("alias").addText(alias);
        }
    }

    private void handleCategories(DynamicContentMacroModuleBean bean, DOMElement element)
    {
        for (MacroCategory category : bean.getCategories())
        {
            element.addElement("category").addText(category.toString());
        }
    }

    private void handleParameters(DynamicContentMacroModuleBean bean, DOMElement element)
    {
        Element parameters = element.addElement("parameters");
        for (MacroParameterBean parameterBean : bean.getParameters())
        {
            Element parameter = parameters.addElement("parameter")
                .addAttribute("name", parameterBean.getName())
                .addAttribute("type", parameterBean.getType().toString());
            if (parameterBean.getRequired())
            {
                parameter.addAttribute("required", "true");
            }
            if (parameterBean.getMultiple())
            {
                parameter.addAttribute("multiple", "true");
            }
            if (parameterBean.hasDefaultValue())
            {
                parameter.addAttribute("default", parameterBean.getDefaultValue());
            }
            // TODO: values and aliases
        }
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
                    // TODO: Handle context params if needed
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

}
