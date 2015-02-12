package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.fugue.Option;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 *
 */
public interface IFrameRenderStrategy
{
    boolean shouldShow(Map<String, ? extends Object> conditionContext);

    void shouldShowOrThrow(Map<String, Object> conditionContext);

    void render(ModuleContextParameters moduleContextParameters, Writer writer, Option<String> uiParameters) throws IOException;

    void renderAccessDenied(Writer writer) throws IOException;

    String getContentType();

    IFrameRenderStrategy toJsonRenderStrategy();
}
