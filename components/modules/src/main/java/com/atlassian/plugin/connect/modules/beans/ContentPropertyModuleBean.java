package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

/**
 * Content properties are one of the forms of persistence available for Confluence Connect add-ons, allowing you
 * to store key-value pairs against a piece of content. Content Properties are stored as JSON objects, and
 * allow you to track extra information that your add-on needs, without the use of a backing server. Values from these
 * JSON objects can be extracted, indexed and made available to CQL queries.
 *
 *#### Using Content Properties
 * The most simple version of a Content Property doesn't require any descriptor changes. Simply
 * <a href="https://developer.atlassian.com/display/CONFDEV/Content+Properties+in+the+REST+API">use the REST API</a> to
 * store your JSON data against page.
 *
 * To <a href="../fragment/content-property-index-key-configuration.html">integrate with search</a>, you'll need to define some
 * <a href="../fragment/content-property-index-extraction-configuration.html">extractions</a> to allow whatever fields
 * you store to be indexable. You can also optionally define an <a href="../fragment/content-property-index-key-configuration.html#alias">alias</a>
 * for simpler CQL querying, and <a href="../fragment/user-interface-support.html">UI support</a> for your fields to be filterable by
 * users on the search screen and in the CQL query builder.
 *
 * Once you've done that, you can use <a href="https://developer.atlassian.com/display/CONFDEV/Advanced+Searching+using+CQL">CQL</a>
 * to query content based on your custom content property.
 *
 *#### Example
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_EXAMPLE}
 * @schemaTitle Content Property
 * @since 1.0
 */
public class ContentPropertyModuleBean extends RequiredKeyBean
{
    /**
     * A Content Property Index Key Configuration defines which values from your JSON content property
     * object should be indexed and made available to the CQL search syntax.
     */
    private List<ContentPropertyIndexKeyConfigurationBean> keyConfigurations;

    public ContentPropertyModuleBean()
    {
    }

    public ContentPropertyModuleBean(ContentPropertyModuleBeanBuilder contentPropertyModuleBeanBuilder)
    {
        super(contentPropertyModuleBeanBuilder);
        keyConfigurations = contentPropertyModuleBeanBuilder.getKeyConfigurations();
    }

    public List<ContentPropertyIndexKeyConfigurationBean> getKeyConfigurations()
    {
        return keyConfigurations;
    }

    public static ContentPropertyModuleBeanBuilder newContentPropertyModuleBean()
    {
        return new ContentPropertyModuleBeanBuilder();
    }
}
