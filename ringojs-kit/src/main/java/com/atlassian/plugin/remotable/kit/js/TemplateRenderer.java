package com.atlassian.plugin.remotable.kit.js;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.osgi.bridge.external.PluginRetrievalService;
import com.samskivert.mustache.Mustache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Used by atlassian/util
 */
public class TemplateRenderer
{
    private enum TemplateExtension
    {
        mustache, mu;

        public static boolean contains(String value)
        {
            for (TemplateExtension ext : TemplateExtension.values())
            {
                if (ext.name().equals(value))
                {
                    return true;
                }
            }
            return false;
        }
    }

    private final Plugin plugin;

    public TemplateRenderer(PluginRetrievalService pluginRetrievalService)
    {
        plugin = pluginRetrievalService.getPlugin();
    }

    public String render(String path, Map<String,Object> context)
    {
        String realPath = resolvePath(path);

        if (realPath != null)
        {
            String extension = realPath.substring(realPath.lastIndexOf('.') + 1);
            return getRenderer(extension).render(realPath, context);
        }
        else
        {
            throw new IllegalArgumentException("No acceptable template file found for path '" + path + "'");
        }
    }

    public boolean canRender(String path)
    {
        return resolvePath(path) != null;
    }

    private String resolvePath(String path)
    {
        if (path == null) return path;
        String result = null;
        String extension;

        int exti = path.lastIndexOf('.') + 1;
        if (exti > 1 && exti < path.length())
        {
            extension = path.substring(exti);
            if (TemplateExtension.contains(extension))
            {
                if (plugin.getResource(path) != null)
                {
                    result = path;
                }
            }
        }
        else
        {
            for (TemplateExtension ext : TemplateExtension.values())
            {
                extension = ext.name();
                String tryPath = path + "." + extension;
                if (plugin.getResource(tryPath) != null)
                {
                    result = tryPath;
                    break;
                }
            }
        }
        return result;
    }

    private Renderer getRenderer(String extension)
    {
        Renderer renderer;
        if (TemplateExtension.mustache.name().equalsIgnoreCase(extension)
            || TemplateExtension.mu.name().equalsIgnoreCase(extension))
        {
            renderer = new MustacheRenderer();
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized template file extension '" + extension + "'");
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
