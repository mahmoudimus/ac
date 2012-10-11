package com.atlassian.plugin.remotable.container.service.plugins;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceFilter;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.base.Supplier;

import java.io.Writer;

/**
 * NoOp implementation of WebResourceManager that is needed for some bundled plugins
 */
public class NoOpWebResourceManager implements WebResourceManager
{
    @Override
    public void requireResource(String s)
    {
    }

    @Override
    public void includeResources(Iterable<String> strings, Writer writer, UrlMode urlMode)
    {
    }

    @Override
    public void includeResources(Writer writer, UrlMode urlMode)
    {
    }

    @Override
    public void includeResources(Writer writer, UrlMode urlMode,
            WebResourceFilter webResourceFilter)
    {
    }

    @Override
    public String getRequiredResources(UrlMode urlMode)
    {
        return null;
    }

    @Override
    public String getRequiredResources(UrlMode urlMode, WebResourceFilter webResourceFilter)
    {
        return null;
    }

    @Override
    public void requireResource(String s, Writer writer, UrlMode urlMode)
    {
    }

    @Override
    public void requireResourcesForContext(String s)
    {
    }

    @Override
    public String getResourceTags(String s, UrlMode urlMode)
    {
        return null;
    }

    @Override
    public <T> T executeInNewContext(Supplier<T> tSupplier)
    {
        return null;
    }

    @Override
    public String getStaticResourcePrefix(UrlMode urlMode)
    {
        return null;
    }

    @Override
    public String getStaticResourcePrefix(String s, UrlMode urlMode)
    {
        return null;
    }

    @Override
    public String getStaticPluginResource(String s, String s1, UrlMode urlMode)
    {
        return null;
    }

    @Override
    public String getStaticPluginResource(ModuleDescriptor<?> moduleDescriptor, String s,
            UrlMode urlMode)
    {
        return null;
    }

    @Override
    public void includeResources(Writer writer)
    {
    }

    @Override
    public String getRequiredResources()
    {
        return null;
    }

    @Override
    public void requireResource(String s, Writer writer)
    {
    }

    @Override
    public String getResourceTags(String s)
    {
        return null;
    }

    @Override
    public String getStaticResourcePrefix()
    {
        return null;
    }

    @Override
    public String getStaticResourcePrefix(String s)
    {
        return null;
    }

    @Override
    public String getStaticPluginResource(String s, String s1)
    {
        return null;
    }

    @Override
    public String getStaticPluginResource(ModuleDescriptor<?> moduleDescriptor, String s)
    {
        return null;
    }

    @Override
    public String getStaticPluginResourcePrefix(ModuleDescriptor<?> moduleDescriptor, String s)
    {
        return null;
    }

    @Override
    public void setIncludeMode(IncludeMode includeMode)
    {
    }
}
