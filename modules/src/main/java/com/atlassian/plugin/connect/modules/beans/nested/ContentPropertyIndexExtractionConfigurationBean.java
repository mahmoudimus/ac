package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.UISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyIndexExtractionConfigurationBeanBuilder;

/**
 * An extraction recipe for a single value within a content property JSON object.
 *
 * An extraction recipe defines which values within your JSON content property will be added to the search
 * index and made available to CQL queries as a field.  This can allow you to track custom information
 * and make it look like a simple field on the content object.
 *
 * You can further improve the field definition by including [UI support](../fragment/ui-support.html).
 *
 * See the [content property key](../fragment/content-property-index-key-configuration.html) documentation for
 * a complete example.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE}
 * @schemaTitle Content Property Index Extraction Configuration
 */
public class ContentPropertyIndexExtractionConfigurationBean extends BaseModuleBean
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

    /**
     * CQL Field name alias for this content property.
     *
     * By defining an alias you are exposing it to CQL and allow other macros and search features to easily use
     * your content property in their search.
     *
     * Note: Aliases are defined globally so take care in your naming strategy and if possible, prefix them with
     * your plugin name.
     */
    private final String alias;

    /**
     * The uiSupport can be used to define how your aliased field will be displayed in the CQL query builder.  Any
     * macro or search feature that uses CQL build to build up the CQL query.
     *
     * By defining uiSupport your content property will appear in the CQL query builder for all macros and search
     * features built on CQL.
     *
     * Note: Requires an alias to be defined.
     */
    private final UISupportModuleBean uiSupport;

    public ContentPropertyIndexExtractionConfigurationBean(ContentPropertyIndexExtractionConfigurationBeanBuilder builder)
    {
        this.objectName = builder.getObjectName();
        this.type = builder.getType();
        this.alias = builder.getAlias();
        this.uiSupport = builder.getUiSupport();
    }

    public static ContentPropertyIndexExtractionConfigurationBeanBuilder newContentPropertyIndexExtractionConfigurationBean()
    {
        return new ContentPropertyIndexExtractionConfigurationBeanBuilder();
    }

    public String getObjectName()
    {
        return objectName;
    }

    public ContentPropertyIndexFieldType getType()
    {
        return type;
    }

    public String getAlias()
    {
        return alias;
    }

    public UISupportModuleBean getUiSupport()
    {
        return uiSupport;
    }
}
