package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.UISupportModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

/**
 * The UI Support element allows you to define how your content property field will be presented in
 * the CQL Builder.
 *
 * When you define UI support for a field, then the CQL builder will include it in all CQL features
 * in Confluence, including other CQL based macros.
 *
 * See the [content property key](../fragment/content-property-index-key-configuration.html) documentation for
 * a complete content property example.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_UI_SUPPORT}
 * @schemaTitle UI Support
 */
@SchemaDefinition("uiSupport")
public class UISupportModuleBean extends BaseModuleBean
{
    /**
     * The CQL builder will use this operator when constructing the CQL string.
     */
    private String defaultOperator;

    /**
     * The name of this field as used by the CQL builder UI components.
     */
    @Required
    private I18nProperty name;

    /**
     * The tooltip of this field as used by the CQL builder UI components.
     */
    private I18nProperty tooltip;

    /**
     * If provided, the CQL builder will provide a drop down and use this url to find the list of value values.  This
     * URL should return a json array of objects describing the valid options.
     *
     * <pre><code>
     * [
     *   {"id": "value1", "text": "First Option"},
     *   {"id": "value2", "text": "Second Option"}
     * ]
     * </code></pre>
     *
     * <strong>NOTE:</strong> since the call to this URL will be made from the user's browser, you need to enable
     * (CORS)[http://www.w3.org/TR/cors/] headers for responses to this resource.
     *
     * Setting a <code>Access-Control-Allow-Origin</code> header to the URL of the Atlassian Cloud instance where
     * this addon is installed is usually enough to satisfy the CORS requirements.  This can be done by tracking
     * the client key and its url in your <code>/installed</code> callback, then looking up the url when the request
     * is made.
     */
    private String dataUri;

    /**
     * As well as providing a text field and allowing any entry, the UI support system provides a number of
     * build in components that can enrich the user experience.  These provide extra user interface components
     * to allow setting or picking their value in an intuitive way.
     *
     * The type can be one of the following values:
     *
     * * `space` - provides a space picker and stores the result space key as the result.
     * * `label` - provides a label picker and stores the list of labels as the result.
     * * `user` - provides a user picker and stores the username as the result.
     * * `contentId` - provides a content picker and stores the content id as the result.
     * * `contentType` - provides a content type picker.
     * * 'date' - provides a date picker
     * * 'string' - provides a free form text field
     * * 'number' - provides a free form text field
     */
    private String valueType;

    public UISupportModuleBean(UISupportModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static UISupportModuleBeanBuilder newUISupportModuleBean()
    {
        return new UISupportModuleBeanBuilder<>();
    }

    public static UISupportModuleBeanBuilder newUISupportModuleBean(UISupportModuleBean defaultBean)
    {
        return new UISupportModuleBeanBuilder(defaultBean);
    }

    public String getDefaultOperator()
    {
        return defaultOperator;
    }

    public I18nProperty getName()
    {
        return name;
    }

    public String getDataUri()
    {
        return dataUri;
    }

    public I18nProperty getTooltip()
    {
        return tooltip;
    }

    public String getValueType()
    {
        return valueType;
    }
}
