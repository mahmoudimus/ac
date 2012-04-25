package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.sal.api.component.ComponentLocator;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.labs.remoteapps.modules.util.redirect.RedirectServlet.getPermanentRedirectUrl;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalUriAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Parent for all macro types
 */
public abstract class AbstractMacroModuleGenerator implements RemoteModuleGenerator
{
    protected final SystemInformationService systemInformationService;
    protected final XhtmlContent xhtmlContent;
    protected final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    protected final MacroContentManager macroContentManager;
    protected final I18NBeanFactory i18NBeanFactory;
    protected final PluginAccessor pluginAccessor;
    protected final MacroMetadataParser macroMetadataParser;

    public AbstractMacroModuleGenerator(
            MacroContentManager macroContentManager, XhtmlContent xhtmlContent,
            I18NBeanFactory i18NBeanFactory,
            ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            SystemInformationService systemInformationService, PluginAccessor pluginAccessor)
    {
        this.macroContentManager = macroContentManager;
        this.xhtmlContent = xhtmlContent;
        this.i18NBeanFactory = i18NBeanFactory;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.systemInformationService = systemInformationService;
        this.pluginAccessor = pluginAccessor;
        this.macroMetadataParser = ComponentLocator.getComponent(MacroMetadataParser.class);
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

                String description = parameter.elementText("description");
                if (!StringUtils.isBlank(description))
                {
                    i18n.put(pluginKey + "." + key + ".param." + parameter.attributeValue("name") + ".desc", description);
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
        config.addAttribute("class", StorageFormatMacro.class.getName());
        if (config.element("parameters") == null)
        {
            config.addElement("parameters");
        }

        URI icon = getOptionalUriAttribute(config, "icon-url");
        if (icon != null)
        {
            config.addAttribute("icon", getPermanentRedirectUrl(
                    ctx.getApplicationType().getId().get(), icon));
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
    public void validate(Element element, String registrationUrl, String username) throws
                                                                                   PluginParseException
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
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
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
                ApplicationLinkOperationsFactory.LinkOperations linkOperations = applicationLinkOperationsFactory.create(
                        ctx.getApplicationType());
                RemoteMacroInfo macroInfo = new RemoteMacroInfo(originalEntity, linkOperations, bodyType,
                        outputType, url);
                RemoteMacro macro = createMacro(macroInfo, ctx);
                if (placeholder != null && Macro.BodyType.NONE.equals(bodyType))
                {
                    return (T) new ImagePlaceholderMacroWrapper(
                            macro,
                            placeholder.applyChrome,
                            placeholder.getDimensions(),
                            placeholder.imageUrl,
                            originalEntity.attributeValue("key"),
                            ctx.getPlugin().getKey());
                }
                else
                {
                    return (T) macro;
                }
            }
        };
        return new XhtmlMacroModuleDescriptor(factory, macroMetadataParser);
    }

    protected abstract RemoteMacro createMacro(RemoteMacroInfo remoteMacroInfo,
            RemoteAppCreationContext ctx);

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
