package com.atlassian.plugin.remotable.plugin.module.page;

import com.atlassian.plugin.remotable.plugin.module.IFrameParams;

import java.net.URI;

public class IFrameContext
{
    private final URI iframePath;

    private final String namespace;
    private final IFrameParams iframeParams;
    private final String pluginKey;

    public IFrameContext(String pluginKey,
                         URI iframePath,
                         String namespace,
                         IFrameParams iframeParams
    )
    {
        this.pluginKey = pluginKey;
        this.iframePath = iframePath;
        this.namespace = namespace;
        this.iframeParams = iframeParams;
    }

    public IFrameContext(IFrameContext iframeContext, String namespaceSuffix)
    {
        this(iframeContext.getPluginKey(),
             iframeContext.getIframePath(),
             iframeContext.getNamespace() + namespaceSuffix,
             iframeContext.getIFrameParams());
    }

    public URI getIframePath()
    {
        return iframePath;
    }

    public String getNamespace()
    {
        return namespace;
    }

    public IFrameParams getIFrameParams()
    {
        return iframeParams;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }
}

