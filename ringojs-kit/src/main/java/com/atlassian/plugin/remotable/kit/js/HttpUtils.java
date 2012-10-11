package com.atlassian.plugin.remotable.kit.js;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.samskivert.mustache.Mustache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;

/**
 * Used by atlassian/renderer
 */
public class HttpUtils
{
    private enum TemplateExtension
    {
        mustache, mu
    }

    private final Plugin plugin;

    public HttpUtils(PluginRetrievalService pluginRetrievalService)
    {
        plugin = pluginRetrievalService.getPlugin();
    }

    // @todo this is all pretty much bogus -- replace with Atlassian Template Renderer service
    public String render(String path, Map<String,Object> context)
    {
        String extension = null;
        URL resource = null;

        int exti = path.lastIndexOf('.') + 1;
        if (exti > 1 && exti < path.length())
        {
            extension = path.substring(exti);
            resource = plugin.getResource(path);
        }
        else
        {
            for (TemplateExtension ext : TemplateExtension.values())
            {
                extension = ext.name();
                String tryPath = path + "." + extension;
                resource = plugin.getResource(tryPath);
                if (resource != null)
                {
                    path = tryPath;
                    break;
                }
            }
        }

        if (resource != null)
        {
            return getRenderer(path, extension).render(path, context);
        }
        else
        {
            throw new IllegalArgumentException("No acceptable template file found for path '" + path + "'");
        }
    }

    private Renderer getRenderer(String path, String extension)
    {
        Renderer renderer;
        if (TemplateExtension.mustache.name().equalsIgnoreCase(extension)
            || TemplateExtension.mu.name().equalsIgnoreCase(extension))
        {
            renderer = new MustacheRenderer();
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized template file extension for path '" + path + "'");
        }
        return renderer;
    }

    private static interface Renderer
    {
        String render(String path, Map<String, Object> context);
    }

    private class MustacheRenderer implements Renderer
    {
        @Override
        public String render(String path, Map<String, Object> context)
        {
            InputStream stream = plugin.getResourceAsStream(path);
            InputStreamReader reader = new InputStreamReader(stream);
            StringWriter writer = new StringWriter();
            try
            {
                Mustache.compiler().compile(reader).execute(context, writer);
            }
            finally
            {
                try
                {
                    stream.close();
                }
                catch (IOException ioe)
                {
                    throw new RuntimeException(ioe);
                }
            }
            return writer.toString();
        }
    }
}
