package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.IFrameParams;

public class IFrameContext
{
    // fixme: verify all these properties are still needed
    private final String iframePath;

    private final String namespace;
    private final IFrameParams iframeParams;
    private final String pluginKey;

    public IFrameContext(String pluginKey,
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

    public IFrameContext(IFrameContext iframeContext, String namespaceSuffix)
    {
        this(iframeContext.getPluginKey(),
             iframeContext.getIframePath(),
             iframeContext.getNamespace() + namespaceSuffix,
             iframeContext.getIFrameParams());
    }

    public String getIframePath()
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

