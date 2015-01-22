package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.List;

/**
 * Representation of a group of extractions from a single JSON documents, associated with a given content property key.
 */
public class ContentPropertyIndexKeyConfigurationBean
{
    private final String propertyKey;
    private final List<ContentPropertyIndexExtractionConfigurationBean> extractions;

    public ContentPropertyIndexKeyConfigurationBean(String propertyKey,
            List<ContentPropertyIndexExtractionConfigurationBean> extractions)
    {
        this.propertyKey = propertyKey;
        this.extractions = extractions;
    }

    public String getPropertyKey()
    {
        return propertyKey;
    }

    public List<ContentPropertyIndexExtractionConfigurationBean> getExtractions()
    {
        return extractions;
    }
}
