package com.atlassian.plugin.connect.api.web;

import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class WebFragmentContext {
    private final Map<String, ?> productContext;
    private final Map<String, ?> connectContext;

    public WebFragmentContext(final Map<String, ?> productContext, final Map<String, ?> connectContext) {
        this.productContext = productContext;
        this.connectContext = connectContext;
    }

    public Map<String, ?> getProductContext() {
        return productContext;
    }

    public Map<String, ?> getConnectContext() {
        return connectContext;
    }

    public static WebFragmentContext from(final ModuleContextParameters moduleContextParameters) {
        Map<String, Object> connectContext = moduleContextParameters.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new WebFragmentContext(moduleContextParameters.getOriginalContext(), connectContext);
    }
}
