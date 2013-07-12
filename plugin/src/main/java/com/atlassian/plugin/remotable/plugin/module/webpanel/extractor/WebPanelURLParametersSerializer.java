package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
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

    public ImmutableMap<String, String[]> getExtractedWebPanelParameters(final Map<String, Object> context)
    {
        final ImmutableMap.Builder<String, String[]> builder = ImmutableMap.builder();
        for (WebPanelParameterExtractor extractor : webPanelParameterExtractors)
        {
            final Optional<Map.Entry<String,String[]>> extractedParameters = extractor.extract(context);
            if (extractedParameters.isPresent())
            {
                builder.put(extractedParameters.get().getKey(), extractedParameters.get().getValue());
            }
        }
        return builder.build();
    }
}
