package com.atlassian.plugin.remotable.kit.js.ringojs;

import com.atlassian.plugin.remotable.kit.js.ringojs.repository.CoffeeScriptCompiler;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 *
 */
public class BundleResource extends AbstractResource
{
    private int exists = -1;
    private final Bundle bundle;
    private static final CoffeeScriptCompiler compiler = new CoffeeScriptCompiler("1.3.3", true);


    protected BundleResource(Bundle bundle, BundleRepository repository, String name) {
        this.bundle = bundle;
        this.repository = repository;
        this.name = name;
        this.path = repository.getPath() + name;
        setBaseNameFromName(name);
    }

    public long lastModified() {
        return repository.lastModified();
    }

    public boolean exists() {
        if (exists < 0) {
            exists = getUrl() != null ? 1 : 0;
        }
        return exists == 1;
    }

    public long getLength() {
        return 0;
    }

    public InputStream getInputStream() throws IOException
    {
        URL url = getUrl();
        if (url != null)
        {
            if (url.getPath().endsWith(".coffee"))
            {
                InputStream in = null;
                try
                {
                    in = url.openStream();
                    String source = IOUtils.toString(in);
                    return new ByteArrayInputStream(
                            compiler.compile(source).getBytes(Charset.defaultCharset())
                    );
                }
                finally
                {
                    IOUtils.closeQuietly(in);
                }
            }
            else
            {
                return stripShebang(url.openStream());
            }
        }
        else
        {
            return null;
        }
    }

    public URL getUrl() {
        URL url =  bundle.getResource(path);
        if (url == null)
        {
            url = bundle.getResource(repository.getPath() + baseName + ".coffee");
        }
        return url;
    }

    @Override
    public String toString() {
        return "BundleResource[" + path + "]";
    }


    @Override
    public int hashCode() {
        return 37 + path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BundleResource && path.equals(((BundleResource)obj).path);
    }
}
