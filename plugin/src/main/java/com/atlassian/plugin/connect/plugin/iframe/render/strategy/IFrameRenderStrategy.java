package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface IFrameRenderStrategy
{
    void preProcessRequest(HttpServletRequest request);

    void render(ModuleContextParameters moduleContextParameters, OutputStream outputStream) throws IOException;
}
