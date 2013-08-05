package com.atlassian.plugin.remotable.spi.module;

public interface IFrameContext
{
    String getIframePath();

    String getNamespace();

    IFrameParams getIFrameParams();

    String getPluginKey();
}
