package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.connect.plugin.module.page.PageInfo;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public interface IFrameRenderer
{
    String render(IFrameContext iframeContext, String remoteUser) throws IOException;

    String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException;

    String renderInline(IFrameContext iframeContext, String extraPath, Map<String,  String[]> queryParams, String remoteUser) throws IOException;

    void renderPage(IFrameContext iframeContext, PageInfo pageInfo, String extraPath, Map<String, String[]> queryParams, String remoteUser, Writer writer) throws IOException;
}
