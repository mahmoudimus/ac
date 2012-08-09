package com.atlassian.labs.remoteapps.api;

import org.dom4j.Document;

import java.net.URL;

/**
 * Descriptor accessor with a static document
 */
public abstract class TransformingDescriptorAccessor implements DescriptorAccessor
{
    private final DescriptorAccessor delegate;

    public TransformingDescriptorAccessor(DescriptorAccessor delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public Document getDescriptor()
    {
        return transform(delegate.getDescriptor());
    }

    @Override
    public String getKey()
    {
        return delegate.getKey();
    }

    @Override
    public URL getDescriptorUrl()
    {
        return delegate.getDescriptorUrl();
    }

    protected abstract Document transform(Document document);
}
