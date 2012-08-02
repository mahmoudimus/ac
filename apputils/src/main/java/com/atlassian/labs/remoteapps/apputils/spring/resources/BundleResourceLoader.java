package com.atlassian.labs.remoteapps.apputils.spring.resources;

import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.atlassian.fugue.Option.*;
import static com.google.common.base.Preconditions.*;

/**
 * Loads resources from a given {@link Bundle OSGi bundle}
 */
public final class BundleResourceLoader implements ResourceLoader
{
    private final Bundle bundle;

    public BundleResourceLoader(Bundle bundle)
    {
        this.bundle = checkNotNull(bundle);
    }

    @Override
    public Option<InputStream> load(String resource)
    {
        return option(bundle.getEntry(resource)).map(new Function<URL, InputStream>()
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
