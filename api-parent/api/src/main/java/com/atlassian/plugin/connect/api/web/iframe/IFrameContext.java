package com.atlassian.plugin.connect.api.web.iframe;

public interface IFrameContext
{
    String getIframePath();

    String getNamespace();

    IFrameParams getIFrameParams();

    String getPluginKey();
}
