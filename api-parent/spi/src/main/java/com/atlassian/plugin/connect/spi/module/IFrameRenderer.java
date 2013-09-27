package com.atlassian.plugin.connect.spi.module;

import com.atlassian.plugin.connect.plugin.module.page.PageInfo;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public interface IFrameRenderer
{
    /**
     * Renders the block container for an iFrame into the containing page
     * @param iframeContext configuration for a plugins iframe
     * @param remoteUser
     * @return
     * @throws IOException
     */
    String render(IFrameContext iframeContext, String remoteUser) throws IOException;

    /**
     * Renders the block container for an iFrame into the containing page
     * @param iframeContext
     * @param extraPath
     * @param queryParams
     * @param remoteUser
     * @return
     * @throws IOException
     */
    String render(IFrameContext iframeContext, String extraPath, Map<String, String[]> queryParams, String remoteUser) throws IOException;

    /**
     * Renders the inline container for an iFrame into the containing page
     * @param iframeContext
     * @param extraPath
     * @param queryParams
     * @param remoteUser
     * @return
     * @throws IOException
     */
    String renderInline(IFrameContext iframeContext, String extraPath, Map<String,  String[]> queryParams, String remoteUser) throws IOException;

    /**
     * Renders the a full iFrame (i.e. container and body)
     * @param iframeContext configuration for the plugins iframe
     * @param pageInfo information about the page
     * @param extraPath
     * @param queryParams
     * @param remoteUser
     * @param writer
     * @throws IOException
     */
    void renderPage(IFrameContext iframeContext, PageInfo pageInfo, String extraPath, Map<String, String[]> queryParams, String remoteUser, Writer writer) throws IOException;
}
