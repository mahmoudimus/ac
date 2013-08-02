package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Serializes a web-panel's context to URL parameters which will be included in web-panel's iframe URL.
 */
@Component
public class WebPanelURLParametersSerializer
{
    private final List<WebPanelParameterExtractor> webPanelParameterExtractors;

    public WebPanelURLParametersSerializer()
    {
        this(Collections.<WebPanelParameterExtractor>emptyList());
    }

    @Autowired(required = false)
    public WebPanelURLParametersSerializer(List<WebPanelParameterExtractor> webPanelParameterExtractors)
    {
        this.webPanelParameterExtractors = checkNotNull(webPanelParameterExtractors);
    }

    public Map<String, Object> getExtractedWebPanelParameters(final Map<String, Object> context)
    {
        Map<String, Object> whiteListedContext = Maps.newHashMap();
        for (WebPanelParameterExtractor extractor : webPanelParameterExtractors)
        {
            whiteListedContext.putAll(extractor.extract(context));
        }
        return Collections.unmodifiableMap(whiteListedContext);
    }
}
