package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.ImmutableSet;
import org.dom4j.Element;

import java.util.*;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyDescriptorXml;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyOptionalElements;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyRequiredElements;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptySet;

/**
 *
 */
public class MacroModuleGenerator implements RemoteModuleGenerator
{
    private final SystemInformationService systemInformationService;
    private final XhtmlContent xhtmlContent;
    private final ApplicationLinkOperationsFactory applicationLinkOperationsFactory;
    private final MacroContentManager macroContentManager;
    private final I18NBeanFactory i18NBeanFactory;

    public MacroModuleGenerator(SystemInformationService systemInformationService, XhtmlContent xhtmlContent, ApplicationLinkOperationsFactory applicationLinkOperationsFactory, MacroContentManager macroContentManager, I18NBeanFactory i18NBeanFactory)
    {
        this.systemInformationService = systemInformationService;
        this.xhtmlContent = xhtmlContent;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.macroContentManager = macroContentManager;
        this.i18NBeanFactory = i18NBeanFactory;
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
        for (Element parameter : new ArrayList<Element>(element.element("parameters").elements("parameter")))
        {
            String title = parameter.attributeValue("title");
            if (title != null)
            {
                i18n.put(pluginKey + "." + key + ".param." + parameter.attributeValue("name") + ".label", title);
            }
        }
        return i18n;
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element entity)
    {
        final Map<String,String> i18n = newHashMap();
        Element config = copyDescriptorXml(entity);
        String key = getRequiredAttribute(entity, "key");
        config.addAttribute("key", key);
        config.addAttribute("name", key);
        config.addAttribute("class", RemoteMacro.class.getName());
        copyRequiredElements(entity, config, "parameters");
        copyOptionalElements(entity, config, "property-panel");
        copyOptionalElements(entity, config, "category");
        if (config.element("parameters") != null)
        {
            config.addElement("parameters");
        }

        ModuleDescriptor descriptor = createXhtmlMacroModuleDescriptor(ctx, entity);
        descriptor.init(ctx.getPlugin(), config);
        final Set<ModuleDescriptor> descriptors = ImmutableSet.of(descriptor, createDummyWebItemDescriptor(ctx, entity, descriptor.getKey()));
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return descriptors;
            }
        };
    }

    private ModuleDescriptor createDummyWebItemDescriptor(RemoteAppCreationContext ctx,
                                                     Element e,
                                                     String key
    )
    {
        Element config = copyDescriptorXml(e);
        final String webItemKey = "webitem-" + key;
        config.addAttribute("key", webItemKey);
        config.addAttribute("section", "shouldnot/exist");

        config.addElement("label").setText("Does not matter");
        config.addElement("link").
                setText("#");

        ModuleDescriptor descriptor = ctx.getAccessLevel()
                                         .createWebItemModuleDescriptor(ctx.getBundle().getBundleContext());
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }

    private ModuleDescriptor createXhtmlMacroModuleDescriptor(final RemoteAppCreationContext ctx, Element originalEntity)
    {
        final Macro.BodyType bodyType = parseBodyType(originalEntity);
        final Macro.OutputType outputType = parseOutputType(originalEntity);
        final String url = getRequiredAttribute(originalEntity, "url");
        ModuleFactory factory = new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                return (T) new RemoteMacro(xhtmlContent, bodyType, outputType, url, applicationLinkOperationsFactory.create(ctx.getApplicationType()), macroContentManager);
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
}
