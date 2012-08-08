package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.labs.remoteapps.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;
import com.atlassian.labs.remoteapps.modules.IFrameRenderer;
import com.atlassian.labs.remoteapps.modules.WebItemCreator;
import com.atlassian.labs.remoteapps.modules.page.IFrameContext;
import com.atlassian.labs.remoteapps.modules.page.IFramePageServlet;
import com.atlassian.labs.remoteapps.modules.page.PageInfo;
import com.atlassian.labs.remoteapps.util.contextparameter.ContextParameterParser;
import com.atlassian.labs.remoteapps.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.labs.remoteapps.util.uri.Uri;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.atlassian.labs.remoteapps.modules.util.redirect.RedirectServlet
        .getPermanentRedirectUrl;
import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;

/**
 * Creates macro module descriptors.  Builder instances are meant to be shared across instances.
 */
public class MacroModuleDescriptorCreator
{
    public static interface MacroFactory
    {

        RemoteMacro create(RemoteMacroInfo remoteMacroInfo);
    }
    private final SystemInformationService systemInformationService;

    private final MacroMetadataParser macroMetadataParser;
    private final RemoteAppAccessorFactory remoteAppAccessorFactory;
    private final HostContainer hostContainer;
    private final ServletModuleManager servletModuleManager;
    private final WebItemCreator webItemCreator;
    private final ContextParameterParser contextParameterParser;
    private final IFrameRenderer iFrameRenderer;
    private final UserManager userManager;

    public MacroModuleDescriptorCreator(SystemInformationService systemInformationService,
            RemoteAppAccessorFactory remoteAppAccessorFactory, HostContainer hostContainer,
            ServletModuleManager servletModuleManager, WebItemCreator webItemCreator,
            ContextParameterParser contextParameterParser, IFrameRenderer iFrameRenderer,
            UserManager userManager)
    {
        this.systemInformationService = systemInformationService;
        this.remoteAppAccessorFactory = remoteAppAccessorFactory;
        this.hostContainer = hostContainer;
        this.servletModuleManager = servletModuleManager;
        this.webItemCreator = webItemCreator;
        this.contextParameterParser = contextParameterParser;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;

        // todo: fix this in confluence
        this.macroMetadataParser = ComponentLocator.getComponent(MacroMetadataParser.class);
    }

    public Builder newBuilder()
    {
        return new Builder();
    }

    public class Builder
    {
        private MacroFactory macroFactory;
        private final WebItemCreator.Builder webItemCreatorBuilder;

        public Builder()
        {
            this.webItemCreatorBuilder = webItemCreator.newBuilder();
            webItemCreatorBuilder.setCondition(new AlwaysDisplayCondition());
        }
        public Builder setMacroFactory(MacroFactory macroFactory)
        {
            this.macroFactory = macroFactory;
            return this;
        }

        public Iterable<ModuleDescriptor> build(Plugin plugin, Element entity)
        {
            Element config = entity.createCopy();

            RemoteAppAccessor remoteAppAccessor = remoteAppAccessorFactory.get(plugin.getKey());

            // Use the 'key' attribute as the name if no name is specified. The name will be used to uniquely identify the
            // macro within the editor.
            String key = getRequiredAttribute(entity, "key");
            String macroKey = "macro-" + key;
            config.addAttribute("key", macroKey);

            String name = getOptionalAttribute(entity, "title", getOptionalAttribute(entity, "name", key));

            // we treat the key as the macro name, with name being the label
            config.addAttribute("name", key);

            config.addAttribute("i18n-name-key", plugin.getKey() + "." + key + ".label");

            config.addAttribute("class", StorageFormatMacro.class.getName());
            if (config.element("parameters") == null)
            {
                config.addElement("parameters");
            }

            URI icon = getOptionalUriAttribute(config, "icon-url");
            if (icon != null)
            {
                String baseUrl = systemInformationService.getConfluenceInfo().getBaseUrl();
                config.addAttribute("icon", baseUrl + getPermanentRedirectUrl(
                        plugin.getKey(), icon));
            }

            // Generate the required plugin module descriptors for this macro
            Set<ModuleDescriptor> descriptors = new HashSet<ModuleDescriptor>();

            ModuleDescriptor descriptor = createXhtmlMacroModuleDescriptor(plugin, entity);
            descriptor.init(plugin, config);
            descriptors.add(descriptor);

            boolean isFeaturedMacro = Boolean.valueOf(getOptionalAttribute(config, "featured", false));
            if (isFeaturedMacro)
            {
                descriptors.add(createFeaturedMacroDescriptor(plugin, key, entity));
                if (icon != null)
                {
                    descriptors.add(createFeaturedIconWebResource(plugin, remoteAppAccessor, key, icon));
                }
            }

            boolean hasCustomEditor = config.element("macro-editor") != null;
            if (hasCustomEditor)
            {
                descriptors.addAll(createMacroEditorDescriptors(plugin, config, key, name));
            }

            return ImmutableSet.copyOf(descriptors);
        }

        private Collection<ModuleDescriptor> createMacroEditorDescriptors(final Plugin plugin, Element config, String macroKey, String macroName)
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
                String msg = String.format("Custom editor URL %s for macro %s is invalid.", originalUrl, macroKey);
                throw new PluginParseException(msg, e);
            }

            // Generate a servlet module descriptor that can redirect requests from the Confluence front-end to the Remote App,
            // performing the necessary authentication.
            String localUrl = "/remoteapps/" + plugin.getKey() + "/" + macroKey + "-editor";
            final ServletModuleDescriptor iFrameServlet = createMacroEditorServletDescriptor(plugin, macroEditor, macroKey, originalUrl, localUrl);

            // Generate a new web-resource module descriptor with the necessary JavaScript to configure the custom macro editor
            // in the Confluence editor.
            ModuleDescriptor jsDescriptor = createCustomEditorWebResource(plugin, macroEditor, macroKey,
                    macroName, localUrl);

            return Lists.newArrayList(jsDescriptor, iFrameServlet);
        }

        private ModuleDescriptor createFeaturedMacroDescriptor(final Plugin plugin, String macroKey, Element macroConfig)
        {
            String name = macroConfig.attributeValue("name");
            Element webItem = DocumentHelper.createDocument().addElement("web-item")
                    .addAttribute("name", name)
                    .addAttribute("key", "editor-featured-macro-" + macroKey)
                    .addAttribute("section", "system.editor.featured.macros.default")
                    .addElement("label")
                    .addText(name).getParent()
                    .addElement("link")
                    .addAttribute("linkId", macroKey).getParent();

            if (macroConfig.attribute("icon-url") != null)
            {
                webItem.addAttribute("icon-url", macroConfig.attributeValue("icon-url"));
            }

            return webItemCreatorBuilder.build(plugin, macroKey, "", webItem);
        }

        private ServletModuleDescriptor createMacroEditorServletDescriptor(final Plugin plugin,
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
                            new IFrameContext(plugin.getKey(), path, moduleKey, params), userManager
                    );
                }
            }, servletModuleManager);
            descriptor.init(plugin, config);
            return descriptor;
        }

        private ModuleDescriptor createCustomEditorWebResource(Plugin plugin,
                Element macroEditorConfig, String macroKey, String macroName,
                String customEditorLocalUrl)
        {
            Element webResource = DocumentHelper.createDocument()
                    .addElement("web-resource")
                    .addAttribute("key", macroKey + "-macro-editor-resources");

            webResource.addElement("resource")
                    .addAttribute("type", "download")
                    .addAttribute("name", "macro-override.js")
                    .addAttribute("location", "js/confluence/macro/macro-override.js");

            webResource.addElement("dependency")
                    .setText("confluence.web.resources:ajs");

            webResource.addElement("context")
                    .setText("editor");

            Element transformation = webResource.addElement("transformation")
                    .addAttribute("extension", "js");

            transformation
                    .addElement("transformer")
                    .addAttribute("key", "macroVariableTransformer")
                    .addElement("var")
                    .addAttribute("name", "MACRONAME")
                    .addAttribute("value", macroKey).getParent()
                    .addElement("var")
                    .addAttribute("name", "URL")
                    .addAttribute("value", "/plugins/servlet" + customEditorLocalUrl).getParent()
                    .addElement("var")
                    .addAttribute("name", "WIDTH")
                    .addAttribute("value", getOptionalAttribute(macroEditorConfig, "width", "")).getParent()
                    .addElement("var")
                    .addAttribute("name", "HEIGHT")
                    .addAttribute("value", getOptionalAttribute(macroEditorConfig, "height", "")).getParent()
                    .addElement("var")
                    .addAttribute("name", "EDIT_TITLE")
                    .addAttribute("value", macroName)
                    .addAttribute("i18n-key", "macro.browser.edit.macro.title").getParent()
                    .addElement("var")
                    .addAttribute("name", "INSERT_TITLE")
                    .addAttribute("value", macroName)
                    .addAttribute("i18n-key", "macro.browser.insert.macro.title").getParent();

            ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
            jsDescriptor.init(plugin, webResource);

            return jsDescriptor;
        }

        private ModuleDescriptor createFeaturedIconWebResource(Plugin plugin, RemoteAppAccessor remoteAppAccessor,
                String macroKey, URI iconUrl)
        {
            Element webResource = DocumentHelper.createDocument()
                    .addElement("web-resource")
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
                    .addAttribute("key", "macroVariableTransformer")
                    .addElement("var")
                    .addAttribute("name", "KEY")
                    .addAttribute("value", macroKey).getParent()
                    .addElement("var")
                    .addAttribute("name", "ICON_URL")
                    .addAttribute("value",
                            remoteAppAccessor.getDisplayUrl() + iconUrl.toString()).getParent();

            ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
            jsDescriptor.init(plugin, webResource);

            return jsDescriptor;
        }

        private ModuleDescriptor createXhtmlMacroModuleDescriptor(final Plugin plugin, final Element originalEntity)
        {
            final Macro.BodyType bodyType = parseBodyType(originalEntity);
            final Macro.OutputType outputType = parseOutputType(originalEntity);
            final URI url = getRequiredUriAttribute(originalEntity, "url");
            final RequestContextParameterFactory requestContextParameterFactory =
                    contextParameterParser.parseContextParameters(originalEntity);

            final ImagePlaceholderConfig placeholder = parseImagePlaceholder(originalEntity);

            ModuleFactory factory = new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {
                    RemoteMacroInfo macroInfo = new RemoteMacroInfo(originalEntity, plugin.getKey(), bodyType,
                            outputType, requestContextParameterFactory, url.getPath());
                    RemoteMacro macro = macroFactory.create(macroInfo);
                    if (placeholder != null && Macro.BodyType.NONE.equals(bodyType))
                    {
                        return (T) new ImagePlaceholderMacroWrapper(
                                macro,
                                placeholder.applyChrome,
                                placeholder.getDimensions(),
                                placeholder.imageUrl,
                                originalEntity.attributeValue("key"),
                                plugin.getKey());
                    }
                    else
                    {
                        return (T) macro;
                    }
                }
            };
            return new FixedXhtmlMacroModuleDescriptor(factory, macroMetadataParser);
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
            URI url = getRequiredUriAttribute(placeholder, "url");
            String width = getOptionalAttribute(placeholder, "width", null);
            String height = getOptionalAttribute(placeholder, "height", null);
            String applyChrome = getOptionalAttribute(placeholder, "apply-chrome", null);

            return new ImagePlaceholderConfig(url,
                    width == null ? null : Integer.parseInt(width),
                    height == null ? null : Integer.parseInt(height),
                    applyChrome == null || Boolean.parseBoolean(applyChrome)); // applyChrome defaults to true
        }

        private class ImagePlaceholderConfig
        {
            URI imageUrl;
            Integer width;
            Integer height;
            boolean applyChrome;

            private ImagePlaceholderConfig(URI imageUrl, Integer width, Integer height, boolean applyChrome)
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
}
