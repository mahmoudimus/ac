package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;

/**
 * Representation of a extraction recipe for a single JSON value. For more information,
 * please see the [Confluence documentation on content properties](https://developer.atlassian.com/display/CONFDEV/Content+Properties+in+the+REST+API).
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_EXAMPLE}
 * @schemaTitle Content Property Index
 */
public class ContentPropertyIndexExtractionConfigurationBean
{
    /**
     * The objectName to the JSON data which is supposed to be indexed. The objectName will be the key of a flatten JSON object with '.' as the delimiter.
     *
     * For instance, for JSON <code>"{"label": {"color": "red", "text":"connect"}}</code> the valid objectName
     * referencing the color is label.color.
     *
     * Currently, specifying of index for JSON arrays is not supported.
     */
    @Required
    private final String objectName;
    /**
     * The type of the referenced value.
     *
     * The type can be one of the following values:
     *
     * * `number` - Extracted number will be indexed as a double value for efficient range filtering and sorting.
     * * `text` - Extracted value will be tokenized before indexing, allowing searching for a particular words.
     * * `string` - Entire extracted value will be indexed as a single token, without any filtering. When extraction
     * expression evaluates to a JSON array, each element will be indexed separately. Enables searching for an exact value, e.g. unique identifier.
     * * `date` - Two representation are possible, either a String following the ISO 8601 datetime format,
     * or a long value in the Unix time. Enables efficient range filtering and sorting.
     *
     */
    @Required
    private final ContentPropertyIndexFieldType type;

    public ContentPropertyIndexExtractionConfigurationBean(String objectName, ContentPropertyIndexFieldType type)
    {
        this.objectName = objectName;
        this.type = type;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public ContentPropertyIndexFieldType getType()
    {
        return type;
    }
}
