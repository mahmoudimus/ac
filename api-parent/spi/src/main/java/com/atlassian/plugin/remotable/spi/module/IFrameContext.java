package com.atlassian.plugin.remotable.spi.module;

import java.net.URI;

public interface IFrameContext
{
    URI getIframePath();

    String getNamespace();

    IFrameParams getIFrameParams();

    String getPluginKey();
}
