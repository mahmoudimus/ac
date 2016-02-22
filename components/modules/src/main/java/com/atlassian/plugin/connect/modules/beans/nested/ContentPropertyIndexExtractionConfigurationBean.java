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
 * You can further extend the field definition by including
 * <a href="../fragment/user-interface-support.html">UI support</a>.
 *
 * See the <a href="../fragment/content-property-index-key-configuration.html">content property key</a> documentation for
 * a complete example.
 *
 * <h3>Example</h3>
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_INDEX_EXTRACTION_CONFIGURATION_EXAMPLE}
 * @schemaTitle Content Property Index Extraction Configuration
 */
public class ContentPropertyIndexExtractionConfigurationBean extends BaseModuleBean {
    /**
     * The <code>objectName</code> of the JSON data which should be indexed. The objectName is the key of a flattened JSON object with '.' as the path separator.
     *
     * For instance, for JSON <code>"{"label": {"color": "red", "text":"connect"}}</code> the valid objectName
     * referencing the color is label.color.
     *
     * Currently, specifying indexes for JSON arrays is not supported.
     */
    @Required
    private final String objectName;

    /**
     * The type of the referenced value.
     *
     * The type can be one of the following values:
     *
     * * <code>number</code> - The extracted number will be indexed as a double value for efficient range filtering and sorting.
     * * <code>text</code> - The extracted value will be tokenized before indexing, allowing searching for particular words.
     * * <code>string</code> - The entire extracted value will be indexed as a single token, without any filtering. When the extraction
     * expression evaluates to a JSON array, each element will be indexed separately. This Enables searching for an exact value, e.g. a unique identifier.
     * * <code>date</code> - Two representations are possible: either a string following the ISO 8601 datetime format,
     * or a long value in Unix time. This enables efficient range filtering and sorting.
     */
    @Required
    private final ContentPropertyIndexFieldType type;

    /**
     * A CQL field name alias for this content property.
     *
     * By defining an alias you are exposing it to CQL and allow other macros and search features to easily use
     * your content property in their search.
     *
     * <strong>Important:</strong> Aliases must be globally unique. Prefixing it with the name of your add-on is the best way to ensure this.
     */
    private final String alias;

    /**
     * <code>uiSupport</code> can be used to define how your aliased field will be displayed in the CQL query builder.
     * By defining <code>uiSupport</code>, your content property will appear in the CQL query builder for all macros and search
     * features built on CQL. For example, your property will become usable in the
     * <a href="https://confluence.atlassian.com/doc/content-by-label-macro-145566.html">Content By Label macro</a>, and
     * filterable by users on the Confluence search screen.
     *
     * Note: You need to define an <code>alias</code> to use <code>uiSupport</code>.
     */
    private final UISupportModuleBean uiSupport;

    public ContentPropertyIndexExtractionConfigurationBean(ContentPropertyIndexExtractionConfigurationBeanBuilder builder) {
        this.objectName = builder.getObjectName();
        this.type = builder.getType();
        this.alias = builder.getAlias();
        this.uiSupport = builder.getUiSupport();
    }

    public static ContentPropertyIndexExtractionConfigurationBeanBuilder newContentPropertyIndexExtractionConfigurationBean() {
        return new ContentPropertyIndexExtractionConfigurationBeanBuilder();
    }

    public String getObjectName() {
        return objectName;
    }

    public ContentPropertyIndexFieldType getType() {
        return type;
    }

    public String getAlias() {
        return alias;
    }

    public UISupportModuleBean getUiSupport() {
        return uiSupport;
    }
}
