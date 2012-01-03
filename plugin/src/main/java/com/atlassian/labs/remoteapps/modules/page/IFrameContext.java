package com.atlassian.labs.remoteapps.modules.page;

import com.atlassian.labs.remoteapps.modules.ApplicationLinkOperationsFactory;

import java.util.Collections;
import java.util.Map;

public class IFrameContext
{

    private final ApplicationLinkOperationsFactory.LinkOperations linkOps;
    private final String iframePath;

    private final String moduleKey;
    private final Map<String, Object> templateParams;

    public IFrameContext(ApplicationLinkOperationsFactory.LinkOperations linkOps,
                         String iframePath,
                         String moduleKey,
                         Map<String, Object> templateParams
    )
    {
        this.linkOps = linkOps;
        this.iframePath = iframePath;
        this.moduleKey = moduleKey;
        this.templateParams = templateParams;
    }

    public ApplicationLinkOperationsFactory.LinkOperations getLinkOps()
    {
        return linkOps;
    }

    public String getIframePath()
    {
        return iframePath;
    }

    public String getModuleKey()
    {
        return moduleKey;
    }

    public Map<String, Object> getTemplateParams()
    {
        return templateParams;
    }
}

