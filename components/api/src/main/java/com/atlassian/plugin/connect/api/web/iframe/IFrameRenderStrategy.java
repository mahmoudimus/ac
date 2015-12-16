package com.atlassian.plugin.connect.api.web.iframe;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

public interface IFrameRenderStrategy
{
    boolean shouldShow(Map<String, ? extends Object> conditionContext);

    void shouldShowOrThrow(Map<String, Object> conditionContext);

    void render(ModuleContextParameters moduleContextParameters, Writer writer, Optional<String> uiParameters) throws IOException;

    void renderAccessDenied(Writer writer) throws IOException;

    String getContentType();

    IFrameRenderStrategy toJsonRenderStrategy();
}
