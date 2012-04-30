package com.atlassian.labs.remoteapps.modules.confluence;

import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableClasspathResource;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Element;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.*;

import java.util.Collections;

/**
 * Generates the JavaScript content injected into the Confluence "editor" context that registers the custom macro browser
 * behaviour for the desired Remote Macro.
 * <p/>
 * To achieve this, each Remote Macro with a custom editor gets a web-resource module generated that is handled by this
 * transformer. This transformer takes a template JavaScript file from the Remote Apps framework plugin (macro-override.js)
 * and replaces the parameter information with the details of the desired macro.
 * <p/>
 * Thus, a system with many Remote Macros installed may have many near-identical copies of this JavaScript in the editor
 * context, each with slightly different variables (We could possibly improve this to reduce duplicate code in the future).
 */
public class MacroEditorInjectorTransformer implements WebResourceTransformer
{
    // TODO: Get the parent plugin key in a less hacky way
    private static final String THIS_PLUGIN_KEY = "com.atlassian.labs.remoteapps-plugin";

    private final Plugin thisPlugin;
    private final I18NBeanFactory userI18NBeanFactory;

    public MacroEditorInjectorTransformer(PluginAccessor pluginAccessor, I18NBeanFactory userI18NBeanFactory)
    {
        this.userI18NBeanFactory = userI18NBeanFactory;
        thisPlugin = pluginAccessor.getPlugin(THIS_PLUGIN_KEY);
    }

    @Override
    public DownloadableResource transform(final Element configElement, final ResourceLocation location, String filePath, final DownloadableResource nextResource)
    {
        final String macroName = getMacroNameFromResourceLocation(location);
        final String macroTitle = getOptionalAttribute(configElement, "label", macroName);
        final String width = getOptionalAttribute(configElement, "width", "");
        final String height = getOptionalAttribute(configElement, "height", "");
        final String url = getRequiredAttribute(configElement, "url");

        return new CharSequenceDownloadableResource(new DownloadableClasspathResource(thisPlugin, getTemplateResourceLocation(), ""))
        {
            @Override
            protected CharSequence transform(CharSequence original)
            {
                String originalS = original.toString();
                originalS = originalS.replace("%%MACRONAME%%", StringEscapeUtils.escapeJavaScript(macroName))
                        .replace("%%WIDTH%%", StringEscapeUtils.escapeJavaScript(width))
                        .replace("%%HEIGHT%%", StringEscapeUtils.escapeJavaScript(height))
                        .replace("%%URL%%", StringEscapeUtils.escapeJavaScript(url))
                        .replace("%%EDIT_TITLE%%", getText("macro.browser.edit.macro.title", StringEscapeUtils.escapeJavaScript(macroTitle)))
                        .replace("%%INSERT_TITLE%%", getText("macro.browser.insert.macro.title", StringEscapeUtils.escapeJavaScript(macroTitle)));
                return originalS;
            }
        };
    }

    private ResourceLocation getTemplateResourceLocation()
    {
        return new ResourceLocation("js/confluence/macro/macro-override.js", "macro-override.js", "js", "application/javascript", "", Collections.<String, String>emptyMap());
    }

    private String getMacroNameFromResourceLocation(ResourceLocation location)
    {
        // NOTE: Kind of a hack.
        // This relies on the fact that we set the file-name of the resource to be identical to the macro name in AbstractMacroModuleGenerator. :-)
        return (location.getName().endsWith(".js")) ? location.getName().substring(0, location.getName().length() - 3) : location.getName();
    }

    private String getText(String key, String... values)
    {
        return userI18NBeanFactory.getI18NBean().getText(key, values);
    }
}
