package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.plugin.descriptor.XhtmlMacroModuleDescriptor;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.labs.remoteapps.modules.*;
import com.atlassian.labs.remoteapps.modules.external.RemoteAppCreationContext;
import com.atlassian.labs.remoteapps.modules.external.RemoteModule;
import com.atlassian.labs.remoteapps.modules.external.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.labs.remoteapps.modules.page.IFramePageServlet;
import com.atlassian.labs.remoteapps.modules.page.PageInfo;
import com.atlassian.labs.remoteapps.product.confluence.ConfluenceProductAccessor;
import com.atlassian.labs.remoteapps.util.Dom4jUtils;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.net.URI;
import java.util.*;

import static com.atlassian.labs.remoteapps.modules.util.redirect.RedirectServlet.getPermanentRedirectUrl;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;
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
    protected final HostContainer hostContainer;
    private final ServletModuleManager servletModuleManager;
    private final WebItemCreator webItemCreator;
    protected final IFrameRenderer iFrameRenderer;
    protected final UserManager userManager;

    public AbstractMacroModuleGenerator(
            MacroContentManager macroContentManager, XhtmlContent xhtmlContent,
            I18NBeanFactory i18NBeanFactory,
            ApplicationLinkOperationsFactory applicationLinkOperationsFactory,
            SystemInformationService systemInformationService, PluginAccessor pluginAccessor,
            HostContainer hostContainer, ServletModuleManager servletModuleManager,
            IFrameRenderer iFrameRenderer, UserManager userManager)
    {
        this.macroContentManager = macroContentManager;
        this.xhtmlContent = xhtmlContent;
        this.i18NBeanFactory = i18NBeanFactory;
        this.applicationLinkOperationsFactory = applicationLinkOperationsFactory;
        this.systemInformationService = systemInformationService;
        this.pluginAccessor = pluginAccessor;
        this.webItemCreator = new WebItemCreator(new InsertMacroWebItemContext(), new ConfluenceProductAccessor());
        this.macroMetadataParser = ComponentLocator.getComponent(MacroMetadataParser.class);
        this.hostContainer = hostContainer;
        this.servletModuleManager = servletModuleManager;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
    }

    @Override
    public Map<String, String> getI18nMessages(String pluginKey, Element element)
    {
        Map<String,String> i18n = newHashMap();
        String name = element.attributeValue("name");
        if (element.element("parameters") != null)
        {
            for (Element parameter : new ArrayList<Element>(element.element("parameters").elements("parameter")))
            {
                String title = parameter.attributeValue("label");
                if (title != null)
                {
                    i18n.put(pluginKey + "." + name + ".param." + parameter.attributeValue("name") + ".label", title);
                }

                String description = parameter.elementText("description");
                if (!StringUtils.isBlank(description))
                {
                    i18n.put(pluginKey + "." + name + ".param." + parameter.attributeValue("name") + ".desc", description);
                }
            }
        }
        if (element.attribute("label") != null)
        {
            i18n.put(pluginKey + "." + name + ".label", element.attributeValue("label"));
        }

        return i18n;
    }

    @Override
    public RemoteModule generate(final RemoteAppCreationContext ctx, Element entity)
    {
        Element config = entity.createCopy();

        // Use the 'key' attribute as the name if no name is specified. The name will be used to uniquely identify the
        // macro within the editor.
        String key = getRequiredAttribute(entity, "key");
        String name = getOptionalAttribute(entity, "name", "");
        if (StringUtils.isBlank(name))
        {
            config.addAttribute("name", key);
            name = key;
        }

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

        // Generate the required plugin module descriptors for this macro
        Set<ModuleDescriptor> descriptors = new HashSet<ModuleDescriptor>();

        ModuleDescriptor descriptor = createXhtmlMacroModuleDescriptor(ctx, entity);
        descriptor.init(ctx.getPlugin(), config);
        descriptors.add(descriptor);

        boolean isFeaturedMacro = Boolean.valueOf(getOptionalAttribute(config, "featured", false));
        if (isFeaturedMacro)
        {
            descriptors.add(createFeaturedMacroDescriptor(ctx, key, name, getOptionalAttribute(config, "label", name)));
        }

        boolean hasCustomEditor = config.element("macro-editor") != null;
        if (hasCustomEditor)
        {
            descriptors.addAll(createMacroEditorDescriptors(ctx, config, key, name));
        }

        final Set<ModuleDescriptor> finalDescriptors = ImmutableSet.copyOf(descriptors);
        return new RemoteModule()
        {
            @Override
            public Set<ModuleDescriptor> getModuleDescriptors()
            {
                return finalDescriptors;
            }
        };
    }

    private Collection<ModuleDescriptor> createMacroEditorDescriptors(final RemoteAppCreationContext ctx, Element config, String remoteModuleKey, String macroName)
    {
        Element macroEditor = config.element("macro-editor");
        String originalUrl = macroEditor.attributeValue("url");
        try
        {
            // Parse into a URI object to validate.
            // NOTE: We could improve this by doing better validation at the XML schema level. This is easier, though :)
            Uri.parse(originalUrl);
        }
        catch (Uri.UriException e)
        {
            String msg = String.format("Custom editor URL %s for macro %s is invalid.", originalUrl, macroName);
            throw new PluginParseException(msg, e);
        }

        // Generate a servlet module descriptor that can redirect requests from the Confluence front-end to the Remote App,
        // performing the necessary authentication.
        String localUrl = "/remoteapps/" + ctx.getApplicationType().getId().get() + "/" + macroName + "-editor";
        final  ServletModuleDescriptor iFrameServlet = createMacroEditorServletDescriptor(ctx, macroEditor, remoteModuleKey, originalUrl, localUrl);

        // Generate a new web-resource module descriptor with the necessary JavaScript to configure the custom macro editor
        // in the Confluence editor.
        ModuleDescriptor jsDescriptor = createWebResourceModuleDescriptor(ctx, macroEditor, remoteModuleKey, macroName, getOptionalAttribute(config, "label", macroName), localUrl);

        return Lists.newArrayList(jsDescriptor, iFrameServlet);
    }

    private ModuleDescriptor createFeaturedMacroDescriptor(final RemoteAppCreationContext ctx, String remoteModuleKey, String macroName, String macroLabel)
    {
        // Create XML config fragment for this web-item
        Element webItem = DocumentHelper.createDocument().addElement("web-item")
                .addAttribute("name", "Insert " + macroLabel); // TODO: Remove hard-coded English

        webItem.addElement("link")
               .addAttribute("linkId", macroName);

        return webItemCreator.createWebItemDescriptor(ctx, webItem, "editor-featured-macro-" + remoteModuleKey, "", new AlwaysDisplayCondition(), "");
    }

    private ServletModuleDescriptor createMacroEditorServletDescriptor(final RemoteAppCreationContext ctx,
                                                                       Element e,
                                                                       final String key,
                                                                       final String path,
                                                                       String localUrl)
    {
        final String moduleKey = "servlet-" + key;
        Element config = e.createCopy()
                            .addAttribute("key", moduleKey)
                            .addAttribute("class", IFramePageServlet.class.getName());
        config.addElement("url-pattern").setText(localUrl + "");
        config.addElement("url-pattern").setText(localUrl + "/*");

        final IFrameParams params = new IFrameParams(e);
        final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                PageInfo pageInfo = new PageInfo("", "-dialog", key, new AlwaysDisplayCondition());

                return (T) new IFramePageServlet(
                        pageInfo,
                        iFrameRenderer,
                        new IFrameContext(applicationLinkOperationsFactory.create(ctx.getApplicationType()), path, moduleKey, params), userManager
                );
            }
        }, servletModuleManager);
        descriptor.init(ctx.getPlugin(), config);
        return descriptor;
    }

    @Override
    public void validate(Element element, String registrationUrl, String username) throws
                                                                                   PluginParseException
    {
        Element root = element.getParent();

        String key = getOptionalAttribute(element, "name", getRequiredAttribute(element, "key"));
        String appKey = root.attributeValue("key");

        for (XhtmlMacroModuleDescriptor descriptor : pluginAccessor.getEnabledModuleDescriptorsByClass(XhtmlMacroModuleDescriptor.class))
        {
            if (key.equals(descriptor.getKey()) && !appKey.equals(descriptor.getPluginKey()))
            {
                throw new PluginParseException("Macro key '" + key + "' already used by app '" + descriptor.getPluginKey() + "'");
            }
        }
        getRequiredUriAttribute(element, "url");
    }

    @Override
    public void generatePluginDescriptor(Element descriptorElement, Element pluginDescriptorRoot)
    {
        // no-op
    }

    private ModuleDescriptor createWebResourceModuleDescriptor(RemoteAppCreationContext ctx, Element macroEditorConfig, String remoteModuleKey, String macroName, String macroLabel, String customEditorLocalUrl)
    {
        Element webResource = DocumentHelper.createDocument()
                                .addElement("web-resource")
                                .addAttribute("key", remoteModuleKey + "-macro-editor-resources");

        webResource.addElement("resource")
                        .addAttribute("type", "download")
                        .addAttribute("name", macroName + ".js")
                        .addAttribute("location", macroName + ".js");

        webResource.addElement("dependency")
                        .setText("confluence.web.resources:ajs");

        webResource.addElement("context")
                        .setText("editor");

        Element transformation = webResource.addElement("transformation")
                                                .addAttribute("extension", "js");

        Element transformer = transformation.addElement("transformer")
                                                .addAttribute("key", "macroEditorTransformer")
                                                .addAttribute("url", "/plugins/servlet" + customEditorLocalUrl)
                                                .addAttribute("label", macroLabel);
        copyOptionalAttribute(macroEditorConfig, transformer, "width");
        copyOptionalAttribute(macroEditorConfig, transformer, "height");

        ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
        jsDescriptor.init(ctx.getPlugin(), webResource);

        return jsDescriptor;
    }

    private ModuleDescriptor createXhtmlMacroModuleDescriptor(final RemoteAppCreationContext ctx, final Element originalEntity)
    {
        final Macro.BodyType bodyType = parseBodyType(originalEntity);
        final Macro.OutputType outputType = parseOutputType(originalEntity);
        final URI url = getRequiredUriAttribute(originalEntity, "url");

        final ImagePlaceholderConfig placeholder = parseImagePlaceholder(originalEntity);

        ModuleFactory factory = new ModuleFactory()
        {
            @Override
            public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
            {
                ApplicationLinkOperationsFactory.LinkOperations linkOperations = applicationLinkOperationsFactory.create(
                        ctx.getApplicationType());
                RemoteMacroInfo macroInfo = new RemoteMacroInfo(originalEntity, linkOperations, bodyType,
                        outputType, url.getPath());
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
        String url = getRequiredUriAttribute(entity, "url").toString();
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
