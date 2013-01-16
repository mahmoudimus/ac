package com.atlassian.plugin.remotable.descriptor;

import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.plugin.remotable.descriptor.DescriptorUtils.*;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Boolean.FALSE;

public final class DevModeTransformingDescriptorAccessor extends DelegatingDescriptorAccessor
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DescriptorAccessor delegate;
    private final LocalMountBaseUrlResolver baseUrlResolver;
    private final RuntimeContext runtimeContext;

    public DevModeTransformingDescriptorAccessor(final DescriptorAccessor delegate,
            LocalMountBaseUrlResolver baseUrlResolver)
    {
        this(delegate, baseUrlResolver, new SystemRuntimeContext());
    }

    public DevModeTransformingDescriptorAccessor(final DescriptorAccessor delegate,
            LocalMountBaseUrlResolver baseUrlResolver, RuntimeContext runtimeContext)
    {
        this.delegate = checkNotNull(delegate);
        this.baseUrlResolver = checkNotNull(baseUrlResolver);
        this.runtimeContext = checkNotNull(runtimeContext);
    }

    private Document transform(Document descriptor)
    {
        final String displayUrl = getDisplayUrl(descriptor);
        if (runtimeContext.isDevMode())
        {
            final String devDisplayUrl = baseUrlResolver.getLocalMountBaseUrl(getKey());
            logger.debug("Replacing set display URL '{}' with new dev URL '{}'", displayUrl, devDisplayUrl);

            return addRegistrationWebHook(
                    addDisplayUrl(getRemotePluginContainerElement(descriptor.getRootElement()), devDisplayUrl).getRootElement());
        }
        else
        {
            return addRegistrationWebHook(descriptor.getRootElement());
        }
    }

    @Override
    public Document getDescriptor()
    {
        return transform(delegate.getDescriptor());
    }

    @Override
    protected DescriptorAccessor getDelegate()
    {
        return delegate;
    }

    static interface RuntimeContext
    {
        boolean isDevMode();
    }

    private static final class SystemRuntimeContext implements RuntimeContext
    {
        @Override
        public boolean isDevMode()
        {
            return Boolean.valueOf(System.getProperty("atlassian.dev.mode", Boolean.toString(FALSE)))
                    || Boolean.valueOf(System.getProperty("atlassian.ub.container.dev.mode", Boolean.toString(FALSE)));
        }
    }
}
