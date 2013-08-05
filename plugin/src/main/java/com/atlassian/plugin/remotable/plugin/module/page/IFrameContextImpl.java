package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.remotable.spi.module.IFrameParams;
import com.atlassian.plugin.remotable.spi.module.IFrameContext;

import java.net.URI;

public final class IFrameContextImpl implements IFrameContext
{
    private final String iframePath;

    private final String namespace;
    private final IFrameParams iframeParams;
    private final String pluginKey;

    public IFrameContextImpl(String pluginKey,
                             URI iframePath,
                             String namespace,
                             IFrameParams iframeParams
    )
    {
        this(pluginKey, iframePath.toString(), namespace, iframeParams);
    }

    public IFrameContextImpl(String pluginKey,
                             String iframePath,
                             String namespace,
                             IFrameParams iframeParams
    )
    {
        this.pluginKey = pluginKey;
        this.iframePath = iframePath;
        this.namespace = namespace;
        this.iframeParams = iframeParams;
    }

    public IFrameContextImpl(IFrameContext iframeContext, String namespaceSuffix)
    {
        this(iframeContext.getPluginKey(),
             iframeContext.getIframePath(),
             iframeContext.getNamespace() + namespaceSuffix,
             iframeContext.getIFrameParams());
    }

    @Override
    public String getIframePath()
    {
        return iframePath;
    }

    @Override
    public String getNamespace()
    {
        return namespace;
    }

    @Override
    public IFrameParams getIFrameParams()
    {
        return iframeParams;
    }

    @Override
    public String getPluginKey()
    {
        return pluginKey;
    }
}

