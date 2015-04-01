package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.List;

import com.atlassian.json.schema.annotation.Required;

/**
 * Representation of a list of extraction recipes for a given content property key. It defines which JSON values
 * should be extracted into the Confluence search index, so that they can be used later on for content search using
 * CQL.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_INDEX_KEY_CONFIGURATION_EXAMPLE}
 * @schemaTitle Content Property Index Key Configuration
 */
public class ContentPropertyIndexKeyConfigurationBean
{
    /**
     * The key of the property from which the data is indexed.
     */
    @Required
    private final String propertyKey;
    /**
     * The list with references to values of JSON object which will be indexed and the types of referenced values.
     */
    @Required
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
