package com.atlassian.plugin.connect.api.web.iframe;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Optional;

public interface IFrameRenderStrategy {
    boolean shouldShow(Map<String, ? extends Object> conditionContext);

    boolean needRedirection();

    void shouldShowOrThrow(Map<String, Object> conditionContext);

    void render(ModuleContextParameters moduleContextParameters, Writer writer, Optional<String> uiParameters) throws IOException;

    void renderAccessDenied(Writer writer) throws IOException;

    String getContentType();

    IFrameRenderStrategy toJsonRenderStrategy();
}
