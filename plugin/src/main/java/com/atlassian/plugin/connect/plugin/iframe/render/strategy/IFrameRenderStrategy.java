package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public interface IFrameRenderStrategy
{
    void render(ModuleContextParameters moduleContextParameters, OutputStream outputStream) throws IOException;
}
