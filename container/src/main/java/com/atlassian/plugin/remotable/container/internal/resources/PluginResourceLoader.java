package com.atlassian.plugin.remotable.container.internal.resources;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.Plugin;
import com.google.common.base.Function;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.atlassian.fugue.Option.*;
import static com.google.common.base.Preconditions.*;

/**
 * Loads resources from a given plugin.  This is used instead of the bundle to allow
 * for dynamic reloading
 */
public final class PluginResourceLoader implements ResourceLoader
{
    private final Plugin plugin;

    public PluginResourceLoader(Plugin plugin)
    {
        this.plugin = checkNotNull(plugin);
    }

    @Override
    public Option<InputStream> load(String resource)
    {
        return option(plugin.getResource(resource)).map(new Function<URL, InputStream>()
        {
            @Override
            public InputStream apply(URL input)
            {
                try
                {
                    return input.openStream();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
