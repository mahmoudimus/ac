package com.atlassian.plugin.connect.plugin.iframe.render.strategy;

import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface IFrameRenderStrategy
{
    void preProcessRequest(HttpServletRequest request);

    boolean shouldShow(Map<String, Object> conditionContext);

    void shouldShowOrThrow(Map<String, Object> conditionContext);

    void render(ModuleContextParameters moduleContextParameters, Writer writer) throws IOException;
}
