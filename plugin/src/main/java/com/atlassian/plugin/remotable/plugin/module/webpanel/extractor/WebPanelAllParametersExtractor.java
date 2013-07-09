package com.atlassian.plugin.remotable.plugin.module.webpanel.extractor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class WebPanelAllParametersExtractor
{
    private final List<WebPanelParameterExtractor> webPanelParameterExtractors;

    public WebPanelAllParametersExtractor()
    {
        this(Collections.<WebPanelParameterExtractor>emptyList());
    }

    @Autowired(required = false)
    public WebPanelAllParametersExtractor(final List<WebPanelParameterExtractor> webPanelParameterExtractors)
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
