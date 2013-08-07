package com.atlassian.plugin.connect.spi.module;

public interface IFrameContext
{
    String getIframePath();

    String getNamespace();

    IFrameParams getIFrameParams();

    String getPluginKey();
}
