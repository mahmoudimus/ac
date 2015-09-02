package com.atlassian.plugin.connect.modules.beans.nested;

import java.util.List;

import com.atlassian.json.schema.annotation.Required;

/**
 * A Content Property Index Key Configuration defines which values from your JSON content property
 * object should be indexed and made available to the CQL search syntax.
 *
 * Each content property key will define one or more
 * <a href="../fragment/content-property-index-extraction-configuration.html">extractions</a> which will
 * allow for multiple values from your JSON content property to be used in CQL.  Each extraction defines a field
 * that will be made available to CQL.
 *
 * In the <a href="https://bitbucket.org/mjensen/wordcount">wordcount</a> example, we store details of the page
 * that describe the word and character counts.
 *
 * After storing this JSON object as a content property:
 *
 * <pre><code>
 * {
 *     "wordCount": 5
 *     "characterCount": 22
 * }
 * </code></pre>
 *
 * We then define a series of extractions to allow access to the 'wordCount' and 'characterCount'
 * properties.
 *
 * <pre><code>
 * {
 *   "propertyKey": "wordcount_addon",
 *   "extractions": [
 *     { "objectName": "wordCount", "type": "number" },
 *     { "objectName": "characterCount", "type": "number" }
 *   ]
 * }
 * </code></pre>
 *
 * You can access this property in your CQL queries as:
 *
 * <pre><code>
 * space = currentSpace() and content.property[wordcount_addon].wordCount &lt;= 1000
 * </code></pre>
 *
 * This is constructed using the following:
 *
 * <pre><code>
 * content.property[<strong>propertyKey</strong>].<strong>objectName</strong>
 * </code></pre>
 *
 * <strong>NOTE:</strong> the <code>propertyKey</code> must be globally unique.  You should prefix
 * it with the name of your plugin to ensure its unique.
 *
 * You can simplify the CQL syntax even further by defining an alias for the extraction:
 *
 * <pre><code>
 * {
 *   "propertyKey": "wordcount_addon",
 *   "extractions": [
 *     { "objectName": "wordCount", "type": "number", alias: "wordcount" }
 *   ]
 * }
 * </code></pre>
 *
 * This allows you to refer to your data using the alias:
 *
 * <pre><code>
 * space = currentSpace() and wordcount &lt;= 1000
 * </code></pre>
 *
 * <strong>NOTE:</strong> the <code>alias</code> must also be globally unique.  You should prefix
 * it with the name of your plugin to ensure its unique.
 *
 * #### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_INDEX_KEY_CONFIGURATION_EXAMPLE}
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
