package com.atlassian.plugin.remotable.kit.js.ringojs.repository;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 *
 */
public class BundleResource extends AbstractResource
{
    private int exists = -1;
    private final Bundle bundle;

    protected BundleResource(Bundle bundle, BundleRepository repository, String name)
    {
        this.bundle = bundle;
        this.repository = repository;
        this.name = name;
        this.path = repository.getPath() + name;
        setBaseNameFromName(name);
    }

    @Override
    public long lastModified()
    {
        return repository.lastModified();
    }

    @Override
    public boolean exists()
    {
        if (exists < 0)
        {
            exists = getUrl() != null ? 1 : 0;
        }
        return exists == 1;
    }

    @Override
    public long getLength()
    {
        return 0;
    }

    @Override
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
                    return compileCoffeeScript(IOUtils.toString(in));
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

    @Override
    public URL getUrl()
    {
        URL url =  bundle.getResource(path);
        if (url == null)
        {
            url = bundle.getResource(repository.getPath() + baseName + ".coffee");
        }
        return url;
    }

    @Override
    public String toString()
    {
        return "BundleResource[" + path + "]";
    }


    @Override
    public int hashCode()
    {
        return 37 + path.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof BundleResource && path.equals(((BundleResource)obj).path);
    }

    @Override
    protected String getCompiledCacheRoot()
    {
        String repoPath = repository.getPath();
        String repoRelPath = repository.getRelativePath();
        return repoPath.substring(0, repoPath.length() - repoRelPath.length());
    }
}
