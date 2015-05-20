package com.atlassian.plugin.connect.spi.iframe.page;

import com.atlassian.plugin.connect.spi.module.IFrameContext;
import com.atlassian.plugin.connect.spi.module.page.PageInfo;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public interface IFramePageRenderer
{
    void renderPage(IFrameContext iframeContext, PageInfo pageInfo, String extraPath, Map<String, String[]> queryParams, String remoteUser, Map<String, Object> productContext, Writer writer) throws IOException;
}
