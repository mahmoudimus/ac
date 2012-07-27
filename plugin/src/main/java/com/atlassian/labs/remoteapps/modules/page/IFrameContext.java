package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.RemoteAppAccessor;
import com.atlassian.labs.remoteapps.RemoteAppAccessorFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;

public class IFrameContext
{

    private final String iframePath;

    private final String namespace;
    private final IFrameParams iframeParams;
    private final RemoteAppAccessor remoteAppAccessor;

    public IFrameContext(RemoteAppAccessor remoteAppAccessor,
                         String iframePath,
                         String namespace,
                         IFrameParams iframeParams
    )
    {
        this.remoteAppAccessor = remoteAppAccessor;
        this.iframePath = iframePath;
        this.namespace = namespace;
        this.iframeParams = iframeParams;
    }

    public IFrameContext(IFrameContext iframeContext, String namespaceSuffix)
    {
        this(iframeContext.getRemoteAppAccessor(),
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

    public RemoteAppAccessor getRemoteAppAccessor()
    {
        return remoteAppAccessor;
    }
}

