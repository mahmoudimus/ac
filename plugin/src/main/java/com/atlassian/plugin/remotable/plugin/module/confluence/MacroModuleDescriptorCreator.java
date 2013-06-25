package com.atlassian.plugin.remotable.plugin.module.confluence;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.pages.thumbnail.Dimensions;
import com.atlassian.confluence.plugin.descriptor.MacroMetadataParser;
import com.atlassian.confluence.status.service.SystemInformationService;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.remotable.plugin.DefaultRemotablePluginAccessorFactory;
import com.atlassian.plugin.remotable.plugin.PermissionManager;
import com.atlassian.plugin.remotable.plugin.module.IFrameParamsImpl;
import com.atlassian.plugin.remotable.plugin.module.IFrameRendererImpl;
import com.atlassian.plugin.remotable.plugin.module.WebItemCreator;
import com.atlassian.plugin.remotable.plugin.module.page.IFrameContextImpl;
import com.atlassian.plugin.remotable.plugin.module.page.IFramePageServlet;
import com.atlassian.plugin.remotable.plugin.module.page.PageInfo;
import com.atlassian.plugin.remotable.plugin.util.contextparameter.ContextParameterParser;
import com.atlassian.plugin.remotable.plugin.util.contextparameter.RequestContextParameterFactory;
import com.atlassian.plugin.remotable.plugin.util.node.Dom4jNode;
import com.atlassian.plugin.remotable.plugin.util.node.Node;
import com.atlassian.plugin.remotable.plugin.util.node.SingleElementWriter;
import com.atlassian.plugin.remotable.spi.Permissions;
import com.atlassian.plugin.remotable.spi.RemotablePluginAccessor;
import com.atlassian.plugin.remotable.plugin.integration.plugins.DescriptorToRegister;
import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.conditions.AlwaysDisplayCondition;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.osgi.framework.BundleContext;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static com.atlassian.plugin.remotable.plugin.module.util.redirect.RedirectServlet.getPermanentRedirectUrl;
import static com.atlassian.plugin.remotable.plugin.util.EncodingUtils.escapeAll;
import static com.atlassian.plugin.remotable.plugin.util.OsgiServiceUtils.getService;
import static com.atlassian.plugin.remotable.spi.util.Dom4jUtils.getOptionalUriAttribute;

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
    private final DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory;
    private final HostContainer hostContainer;
    private final WebItemCreator webItemCreator;
    private final ContextParameterParser contextParameterParser;
    private final IFrameRendererImpl iFrameRenderer;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final BundleContext bundleContext;

    public MacroModuleDescriptorCreator(SystemInformationService systemInformationService,
            DefaultRemotablePluginAccessorFactory remotablePluginAccessorFactory,
            HostContainer hostContainer,
            WebItemCreator webItemCreator,
            ContextParameterParser contextParameterParser,
            IFrameRendererImpl iFrameRenderer,
            UserManager userManager,
            PermissionManager permissionManager,
            BundleContext bundleContext)
    {
        this.systemInformationService = systemInformationService;
        this.remotablePluginAccessorFactory = remotablePluginAccessorFactory;
        this.hostContainer = hostContainer;
        this.webItemCreator = webItemCreator;
        this.contextParameterParser = contextParameterParser;
        this.iFrameRenderer = iFrameRenderer;
        this.userManager = userManager;
        this.permissionManager = permissionManager;
        this.bundleContext = bundleContext;

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
        }
        public Builder setMacroFactory(MacroFactory macroFactory)
        {
            this.macroFactory = macroFactory;
            return this;
        }

        public Iterable<DescriptorToRegister> build(Plugin plugin, Node entity)
        {
            Element config = DocumentHelper.createElement("macro");
            new SingleElementWriter(entity, config)
                    .copyDescription()
                    .copyIfExists("body-type")
                    .copyIfExists("output-type")
                    .copyNodes("alias")
                        .copyText()
                        .done()
                    .copyNodes("category")
                        .copyIfExists("name")
                        .done();

            final RemotablePluginAccessor remotablePluginAccessor = remotablePluginAccessorFactory.get(plugin.getKey());

            // Use the 'key' attribute as the name if no name is specified. The name will be used to uniquely identify the
            // macro within the editor.
            String key = entity.get("key").asString();
            String macroKey = "macro-" + key;
            config.addAttribute("key", macroKey);

            String name = entity.get("title").asString(entity.get("name").asString(key));

            // we treat the key as the macro name, with name being the label
            config.addAttribute("name", key);

            config.addAttribute("i18n-name-key", plugin.getKey() + "." + key + ".label");
            config.addAttribute("system", "true");

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
            Set<DescriptorToRegister> descriptors = new HashSet<DescriptorToRegister>();

            ModuleDescriptor descriptor = createXhtmlMacroModuleDescriptor(plugin, entity);
            descriptor.init(plugin, config);
            descriptors.add(new DescriptorToRegister(descriptor, getI18nMessages(plugin.getKey(), entity)));

            boolean isFeaturedMacro = entity.get("featured").asBoolean(false);
            if (isFeaturedMacro)
            {
                config.addAttribute("featured", "true");
                descriptors.add(createFeaturedMacroDescriptor(plugin, key, entity));
                if (icon != null)
                {
                    descriptors.add(createFeaturedIconWebResource(plugin, remotablePluginAccessor, key, icon));
                }
            }

            boolean hasCustomEditor = entity.get("macro-editor").exists();
            if (hasCustomEditor)
            {
                descriptors.addAll(createMacroEditorDescriptors(plugin, entity, key, name));
            }

            return ImmutableSet.copyOf(descriptors);
        }

        private Properties getI18nMessages(String pluginKey, Node element)
        {
            Properties i18n = new Properties();
            String macroKey = element.get("key").asString();
            if (element.get("parameters").exists())
            {
                for (Node parameter : element.get("parameters").getChildren("parameter"))
                {
                    String paramTitle = parameter.get("title").asString(null);
                    String paramName = parameter.get("name").asString();
                    if (paramTitle != null)
                    {
                        i18n.put(pluginKey + "." + macroKey + ".param." + paramName + ".label", paramTitle);
                    }

                    String description = parameter.get("description").asString(null);
                    if (!StringUtils.isBlank(description))
                    {
                        i18n.put(pluginKey + "." + macroKey + ".param." + paramName + ".desc", description);
                    }
                }
            }
            String macroName = element.get("title").asString(element.get("name").asString(macroKey));
            i18n.put(pluginKey + "." + macroKey + ".label", macroName);

            if (element.get("description").asString(null) != null)
            {
                i18n.put(pluginKey + "." + macroKey + ".desc", element.get("description").asString());
            }

            if (!permissionManager.hasPermission(pluginKey, Permissions.GENERATE_ANY_HTML))
            {
                for (String propName : i18n.stringPropertyNames())
                {
                    i18n.put(propName, escapeAll(i18n.getProperty(propName)));
                }
            }
            return i18n;
        }

        private Collection<DescriptorToRegister> createMacroEditorDescriptors(final Plugin plugin, Node config, String macroKey, String macroName)
        {
            Node macroEditor = config.get("macro-editor");
            URI originalUrl = macroEditor.get("url").asURI();

            // Generate a servlet module descriptor that can redirect requests from the Confluence front-end to the Remotable Plugin,
            // performing the necessary authentication.
            URI localUrl = URI.create("/remotable-plugins/" + plugin.getKey() + "/" + macroKey + "-editor");
            final ServletModuleDescriptor iFrameServlet = createMacroEditorServletDescriptor(plugin, macroEditor, macroKey, originalUrl, localUrl);

            // Generate a new web-resource module descriptor with the necessary JavaScript to configure the custom macro editor
            // in the Confluence editor.
            ModuleDescriptor jsDescriptor = createCustomEditorWebResource(plugin, macroEditor, macroKey,
                    macroName, localUrl);

            return Lists.newArrayList(new DescriptorToRegister(jsDescriptor), new DescriptorToRegister(iFrameServlet));
        }

        private DescriptorToRegister createFeaturedMacroDescriptor(final Plugin plugin, String macroKey, Node macroConfig)
        {
            String name = macroConfig.get("name").asString();
            Element webItem = DocumentHelper.createDocument().addElement("web-item")
                    .addAttribute("name", name)
                    .addAttribute("key", "editor-featured-macro-" + macroKey)
                    .addAttribute("section", "system.editor.featured.macros.default")
                    .addElement("label")
                    .addText(name).getParent()
                    .addElement("link")
                    .addAttribute("linkId", macroKey).getParent();

            if (macroConfig.get("icon-url").exists())
            {
                webItem.addAttribute("icon-url", macroConfig.get("icon-url").asString());
            }

            return new DescriptorToRegister(webItemCreatorBuilder.build(plugin, macroKey, null, new Dom4jNode(webItem)));
        }

        private ServletModuleDescriptor createMacroEditorServletDescriptor(final Plugin plugin,
                Node e,
                final String key,
                final URI path,
                URI localUrl)
        {
            final String moduleKey = "servlet-" + key;
            Element config = DocumentHelper.createElement("servlet")
                    .addAttribute("key", moduleKey)
                    .addAttribute("system", "true")
                    .addAttribute("class", IFramePageServlet.class.getName());
            config.addElement("url-pattern").setText(localUrl + "");
            config.addElement("url-pattern").setText(localUrl + "/*");

            final IFrameParams params = new IFrameParamsImpl(e);
            final ServletModuleDescriptor descriptor = new ServletModuleDescriptor(new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {
                    PageInfo pageInfo = new PageInfo("", "-dialog", key, new AlwaysDisplayCondition(), ImmutableMap.<String, String>of());

                    return (T) new IFramePageServlet(
                            pageInfo,
                            iFrameRenderer,
                            new IFrameContextImpl(plugin.getKey(), path, moduleKey, params), userManager
                    );
                }
            }, getService(bundleContext, ServletModuleManager.class));
            descriptor.init(plugin, config);
            return descriptor;
        }

        private ModuleDescriptor createCustomEditorWebResource(Plugin plugin,
                Node macroEditorConfig, String macroKey, String macroName,
                URI customEditorLocalUrl)
        {
            Element webResource = DocumentHelper.createDocument()
                    .addElement("web-resource")
                    .addAttribute("key", macroKey + "-macro-editor-resources");

            webResource.addElement("resource")
                    .addAttribute("type", "download")
                    .addAttribute("name", "override.js")
                    .addAttribute("location", "js/confluence/macro/override.js");

            webResource.addElement("dependency")
                    .setText("com.atlassian.labs.remoteapps-plugin:ap-amd");

            webResource.addElement("context")
                    .setText("editor");

            Element transformation = webResource.addElement("transformation")
                    .addAttribute("extension", "js");

            transformation
                    .addElement("transformer")
                    .addAttribute("key", "confluence-macroVariableTransformer")
                    .addElement("var")
                    .addAttribute("name", "MACRONAME")
                    .addAttribute("value", macroKey).getParent()
                    .addElement("var")
                    .addAttribute("name", "URL")
                    .addAttribute("value", "/plugins/servlet" + customEditorLocalUrl).getParent()
                    .addElement("var")
                    .addAttribute("name", "WIDTH")
                    .addAttribute("value", macroEditorConfig.get("width").asString("")).getParent()
                    .addElement("var")
                    .addAttribute("name", "HEIGHT")
                    .addAttribute("value", macroEditorConfig.get("height").asString("")).getParent()
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

        private DescriptorToRegister createFeaturedIconWebResource(Plugin plugin, RemotablePluginAccessor remotablePluginAccessor,
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
                    .addAttribute("key", "confluence-macroVariableTransformer")
                    .addElement("var")
                    .addAttribute("name", "KEY")
                    .addAttribute("value", macroKey).getParent()
                    .addElement("var")
                    .addAttribute("name", "ICON_URL")
                    .addAttribute("value",
                            remotablePluginAccessor.getDisplayUrl() + iconUrl.toString()).getParent();

            ModuleDescriptor jsDescriptor = new WebResourceModuleDescriptor(hostContainer);
            jsDescriptor.init(plugin, webResource);

            return new DescriptorToRegister(jsDescriptor);
        }

        private ModuleDescriptor createXhtmlMacroModuleDescriptor(final Plugin plugin, final Node originalEntity)
        {
            final Macro.BodyType bodyType = parseBodyType(originalEntity);
            final Macro.OutputType outputType = parseOutputType(originalEntity);
            final URI url = originalEntity.get("url").asURI();
            final RequestContextParameterFactory requestContextParameterFactory =
                    contextParameterParser.parseContextParameters(originalEntity);

            final ImagePlaceholderConfig placeholder = parseImagePlaceholder(originalEntity);

            ModuleFactory factory = new ModuleFactory()
            {
                @Override
                public <T> T createModule(String name, ModuleDescriptor<T> moduleDescriptor) throws PluginParseException
                {
                    RemoteMacroInfo macroInfo = new RemoteMacroInfo(originalEntity, plugin.getKey(), bodyType,
                            outputType, requestContextParameterFactory, url);
                    RemoteMacro macro = macroFactory.create(macroInfo);
                    if (placeholder != null && Macro.BodyType.NONE.equals(bodyType))
                    {
                        return (T) new ImagePlaceholderMacroWrapper(
                                macro,
                                placeholder.applyChrome,
                                placeholder.getDimensions(),
                                placeholder.imageUrl,
                                plugin.getKey(), userManager);
                    }
                    else
                    {
                        return (T) macro;
                    }
                }
            };
            return new FixedXhtmlMacroModuleDescriptor(factory, macroMetadataParser);
        }

        private Macro.OutputType parseOutputType(Node entity)
        {
            String value = entity.get("output-type").asString("block");
            return Macro.OutputType.valueOf(value.toUpperCase(Locale.US));
        }

        private Macro.BodyType parseBodyType(Node entity)
        {
            Macro.BodyType bodyType;
            String bodyTypeValue = entity.get("body-type").asString("none");
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

        private ImagePlaceholderConfig parseImagePlaceholder(Node entity)
        {
            Node placeholder = entity.get("image-placeholder");
            if (!placeholder.exists())
            {
                return null;
            }
            URI url = placeholder.get("url").asURI();
            String width = placeholder.get("width").asString(null);
            String height = placeholder.get("height").asString(null);
            String applyChrome = placeholder.get("apply-chrome").asString(null);

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
