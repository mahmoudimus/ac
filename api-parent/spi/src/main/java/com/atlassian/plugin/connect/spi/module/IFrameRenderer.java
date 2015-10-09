package com.atlassian.plugin.connect.spi.module;

import java.io.IOException;
import java.util.Map;

public interface IFrameRenderer
{
    String render(IFrameContext iframeContext, String remoteUser) throws IOException;

    String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser, Map<String, Object> productContext) throws IOException;

    String renderInline(IFrameContext iframeContext, String extraPath, Map<String,  String[]> queryParams, String remoteUser, Map<String, Object> productContext) throws IOException;
}
