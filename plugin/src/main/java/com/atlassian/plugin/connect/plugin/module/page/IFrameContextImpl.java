package com.atlassian.plugin.connect.plugin.module.page;

import java.net.URI;

import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.IFrameParams;

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

    @Override
    public String toString()
    {
        return "IFrameContextImpl{" +
                "iframePath='" + iframePath + '\'' +
                ", namespace='" + namespace + '\'' +
                ", iframeParams=" + iframeParams +
                ", pluginKey='" + pluginKey + '\'' +
                '}';
    }
}

