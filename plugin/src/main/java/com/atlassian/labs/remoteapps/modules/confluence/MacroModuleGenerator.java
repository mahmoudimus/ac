package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.GlobalModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptySet;

/**
 *
 */
@GlobalModule
public class MacroModuleGenerator implements RemoteModuleGenerator
{
    private final SystemInformationService systemInformationService;
    private final XhtmlContent xhtmlContent;
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final MacroContentManager macroContentManager;
    private final MacroContentLinkParser macroContentLinkParser;
    private final I18NBeanFactory i18NBeanFactory;
    private final PluginAccessor pluginAccessor;

    public MacroModuleGenerator(SystemInformationService systemInformationService, XhtmlContent xhtmlContent, ApplicationLinkOperationsFactory applicationLinkOperationsFactory, MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory, PluginAccessor pluginAccessor, MacroContentLinkParser macroContentLinkParser)
    {
        this.systemInformationService = systemInformationService;
        this.xhtmlContent = xhtmlContent;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.macroContentManager = macroContentManager;
        this.macroContentLinkParser = macroContentLinkParser;
        this.i18NBeanFactory = i18NBeanFactory;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public String getType()
    {
        return "macro";
    }

    @Override
    public Set<String> getDynamicModuleTypeDependencies()
    {
        return emptySet();
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        Map<String,String> i18n = newHashMap();
        String key = element.attributeValue("key");
        if (element.element("parameters") != null)
        {
            for (Element parameter : new ArrayList<Element>(element.element("parameters").elements("parameter")))
            {
                String title = parameter.attributeValue("title");
                if (title != null)
                {
                    i18n.put(pluginKey + "." + key + ".param." + parameter.attributeValue("name") + ".label", title);
                }
            }
        }
        return i18n;
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element entity)
    {
        Element config = entity.createCopy();

        String key = getRequiredAttribute(entity, "key");
        config.addAttribute("name", key);
        config.addAttribute("class", RemoteMacro.class.getName());
        if (config.element("parameters") == null)
        {
            config.addElement("parameters");
        }

        ModuleDescriptor descriptor = createXhtmlMacroModuleDescriptor(ctx, entity);
        descriptor.init(ctx.getPlugin(), config);
        final Set<ModuleDescriptor> descriptors = ImmutableSet.of(descriptor);
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    @Override
    public void validate(Element element, String registrationUrl, String username) throws PluginParseException
    {
        Element root = element.getParent();

        String key = element.attributeValue("key");
        String appKey = root.attributeValue("key");

        for (XhtmlMacroModuleDescriptor descriptor : pluginAccessor.getEnabledModuleDescriptorsByClass(XhtmlMacroModuleDescriptor.class))
        {
            if (key.equals(descriptor.getKey()) && !appKey.equals(descriptor.getPluginKey()))
            {
                throw new PluginParseException("Macro key '" + key + "' already used by app '" + descriptor.getPluginKey() + "'");
            }
        }
    }

    @Override
    public void convertDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
    }

    private ModuleDescriptor createXhtmlMacroModuleDescriptor(final RemoteAppCreationContext ctx, final Element originalEntity)
    {
        final Macro.BodyType bodyType = parseBodyType(originalEntity);
        final Macro.OutputType outputType = parseOutputType(originalEntity);
        final String url = getRequiredAttribute(originalEntity, "url");

        final ImagePlaceholderConfig placeholder = parseImagePlaceholder(originalEntity);

        ModuleFactory factory = new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                if (placeholder != null && Macro.BodyType.NONE.equals(bodyType))
                {
                    return (T) new ImagePlaceholderRemoteMacro(ctx.getPlugin().getKey(), originalEntity.attributeValue("key"), placeholder.imageUrl, placeholder.getDimensions(),
                            placeholder.applyChrome, xhtmlContent, bodyType, outputType, url, applicationLinkOperationsFactory.create(ctx.getApplicationType()), macroContentManager);
                }
                else
                {
                    return (T) new RemoteMacro(xhtmlContent, bodyType, outputType, url, applicationLinkOperationsFactory.create(ctx.getApplicationType()), macroContentManager);
                }
            }
        };
        return new XhtmlMacroModuleDescriptor(factory, new MacroMetadataParser(systemInformationService, i18NBeanFactory));
    }

    private Macro.OutputType parseOutputType(Element entity)
    {
        String value = getOptionalAttribute(entity, "output-type", "block");
        return Macro.OutputType.valueOf(value.toUpperCase(Locale.US));
    }
    private Macro.BodyType parseBodyType(Element entity)
    {
        Macro.BodyType bodyType;
        String bodyTypeValue = getOptionalAttribute(entity, "body-type", "none");
        if ("rich-text".equals(bodyTypeValue))
        {
            bodyType = Macro.BodyType.RICH_TEXT;
        }
        else if ("plain-text".equals(bodyTypeValue))
        {
            bodyType = Macro.BodyType.PLAIN_TEXT;
        }
        else if ("none".equals(bodyTypeValue))
        {
            bodyType = Macro.BodyType.NONE;
        }
        else
        {
            throw new IllegalArgumentException("Invalid body type '" + bodyTypeValue);
        }
        return bodyType;
    } 

    private ImagePlaceholderConfig parseImagePlaceholder(Element entity)
    {
        Element placeholder = entity.element("image-placeholder");
        if (placeholder == null)
        {
            return null;
        }
        String url = placeholder.attributeValue("url");
        String width = placeholder.attributeValue("width");
        String height = placeholder.attributeValue("height");
        String applyChrome = placeholder.attributeValue("apply-chrome");

        return new ImagePlaceholderConfig(url,
                width == null ? null : Integer.parseInt(width),
                height == null ? null : Integer.parseInt(height),
                applyChrome == null || Boolean.parseBoolean(applyChrome)); // applyChrome defaults to true
    }

    private static class ImagePlaceholderConfig
    {
        String imageUrl;
        Integer width;
        Integer height;
        boolean applyChrome;

        private ImagePlaceholderConfig(String imageUrl, Integer width, Integer height, boolean applyChrome)
        {
            this.imageUrl = imageUrl;
            this.width = width;
            this.height = height;
            this.applyChrome = applyChrome;
        }

        public Dimensions getDimensions()
        {
            if (height != null && width != null)
            {
                return new Dimensions(width, height);
            }
            else
            {
                return null;
            }
        }
    }
}
