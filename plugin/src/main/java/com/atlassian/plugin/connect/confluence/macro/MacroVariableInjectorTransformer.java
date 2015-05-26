package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.util.i18n.I18NBeanFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.elements.ResourceLocation;
import com.atlassian.plugin.servlet.DownloadableClasspathResource;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.CharSequenceDownloadableResource;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformer;
import org.dom4j.Element;

import java.util.List;

import static org.apache.commons.lang.StringEscapeUtils.escapeJavaScript;

/**
 * <p>Transforms content dealing with macros that is injected into the Confluence "editor" context
 * for the desired Remote Macro.</p>
 *
 * <p>It is generally used to process a template resource file to customize it for each macro by
 * replacing parameter information in the form of %%SOME_KEY%%. Keys are defined as child elements
 * on the transformer instance in the form of:</p>
 *
 * <pre>
 *     &lt;var name="SOME_KEY" value="some value" i18-key="some.key"&gt;
 * </pre>
 *
 * <p>The 'i18n-key' attribute is optional and if provided, will cause a runtime lookup of that
 * message key with the value as the single argument.  All values will be Javascript escaped
 * except for css values.</p>
 *
 * <p>Thus, a system with many Remote Macros installed may have many near-identical copies of
 * resources in the editor context, each with slightly different variables (We could possibly
 * improve this to reduce duplicate code in the future).</p>
 */
public class MacroVariableInjectorTransformer implements WebResourceTransformer
{
    private final Plugin thisPlugin;
    private final I18NBeanFactory userI18NBeanFactory;

    public MacroVariableInjectorTransformer(PluginAccessor pluginAccessor,
            I18NBeanFactory userI18NBeanFactory)
    {
        this.userI18NBeanFactory = userI18NBeanFactory;
        Plugin connectPlugin = pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
        if (connectPlugin == null) {
            throw new IllegalStateException("Failed to resolve plugin '" + ConnectPluginInfo.getPluginKey() + "'");
        }
        thisPlugin = connectPlugin;
    }

    @Override
    public DownloadableResource transform(final Element configElement, final ResourceLocation location, String filePath, final DownloadableResource nextResource)
    {
        return new CharSequenceDownloadableResource(new DownloadableClasspathResource(thisPlugin, location, ""))
        {
            @Override
            protected CharSequence transform(CharSequence original)
            {
                String originalS = original.toString();
                for (Element var : (List<Element>) configElement.elements("var"))
                {
                    String value = var.attributeValue("value");
                    if (var.attribute("i18n-key") != null)
                    {
                        value = getText(var.attributeValue("i18n-key"), value);
                    }
                    String escapedValue = location.getLocation().endsWith(".css") ? value :
                        escapeJavaScript(value);
                    originalS = originalS.replace("%%" + var.attributeValue("name") + "%%",
                            escapedValue);
                }

                return originalS;
            }
        };
    }

    private String getText(String key, String... values)
    {
        return userI18NBeanFactory.getI18NBean().getText(key, values);
    }
}
