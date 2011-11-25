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

import java.util.Locale;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyDescriptorXml;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyOptionalElements;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.copyRequiredElements;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getOptionalAttribute;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
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
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element entity)
    {
        Element config = copyDescriptorXml(entity);
        String key = getRequiredAttribute(entity, "key");
        config.addAttribute("key", key);
        config.addAttribute("name", key);
        config.addAttribute("class", RemoteMacro.class.getName());
        copyRequiredElements(entity, config, "parameters");
        copyOptionalElements(entity, config, "property-panel");
        copyOptionalElements(entity, config, "category");

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
