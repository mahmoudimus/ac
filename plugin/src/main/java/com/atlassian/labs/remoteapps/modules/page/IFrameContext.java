package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;
import com.atlassian.labs.remoteapps.modules.IFrameParams;

public class IFrameContext
{

    private final ApplicationLinkOperationsFactory.LinkOperations linkOps;
    private final String iframePath;

    private final String namespace;
    private final IFrameParams iframeParams;

    public IFrameContext(ApplicationLinkOperationsFactory.LinkOperations linkOps,
                         String iframePath,
                         String namespace,
                         IFrameParams iframeParams
    )
    {
        this.linkOps = linkOps;
        this.iframePath = iframePath;
        this.namespace = namespace;
        this.iframeParams = iframeParams;
    }

    public IFrameContext(IFrameContext iframeContext, String namespaceSuffix)
    {
        this(iframeContext.getLinkOps(),
             iframeContext.getIframePath(),
             iframeContext.getNamespace() + namespaceSuffix,
             iframeContext.getIFrameParams());
    }

    public ApplicationLinkOperationsFactory.LinkOperations getLinkOps()
    {
        return linkOps;
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
}

