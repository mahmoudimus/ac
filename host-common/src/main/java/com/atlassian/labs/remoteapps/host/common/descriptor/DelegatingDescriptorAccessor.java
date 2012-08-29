package com.atlassian.labs.remoteapps.host.common.descriptor;

import com.google.common.base.Supplier;
import org.dom4j.Document;

import java.net.URL;

import static com.google.common.base.Suppliers.*;

public abstract class DelegatingDescriptorAccessor implements DescriptorAccessor
{
    private final Supplier<DescriptorAccessor> delegate = memoize(new Supplier<DescriptorAccessor>()
    {
        @Override
        public DescriptorAccessor get()
        {
            return getDelegate();
        }
    });

    @Override
    public Document getDescriptor()
    {
        return delegate.get().getDescriptor();
    }

    @Override
    public String getKey()
    {
        return delegate.get().getKey();
    }

    @Override
    public URL getDescriptorUrl()
    {
        return delegate.get().getDescriptorUrl();
    }

    protected abstract DescriptorAccessor getDelegate();
}
