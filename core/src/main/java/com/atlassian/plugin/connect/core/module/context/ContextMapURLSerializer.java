package com.atlassian.plugin.connect.core.module.context;

import com.atlassian.plugin.connect.spi.module.context.ContextMapParameterExtractor;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Serializes a web-panel's context to URL parameters which will be included in web-panel's iframe URL.
 */
@Component
public class ContextMapURLSerializer
{
    private final List<ContextMapParameterExtractor> contextMapParameterExtractors;

    public ContextMapURLSerializer()
    {
        this(Collections.<ContextMapParameterExtractor>emptyList());
    }

    @Autowired(required = false)
    public ContextMapURLSerializer(
            List<ContextMapParameterExtractor> contextMapParameterExtractors)
    {
        this.contextMapParameterExtractors = checkNotNull(contextMapParameterExtractors);
    }

    public Map<String, Object> getExtractedWebPanelParameters(final Map<String, Object> context)
    {
        Map<String, Object> whiteListedContext = newHashMap();
        for (ContextMapParameterExtractor extractor : contextMapParameterExtractors)
        {
            Optional<Object> option = extractor.extract(context);
            if (option.isPresent())
            {
                whiteListedContext.putAll(extractor.serializer().serialize(option.get()));
            }
        }
        return Collections.unmodifiableMap(whiteListedContext);
    }
}
