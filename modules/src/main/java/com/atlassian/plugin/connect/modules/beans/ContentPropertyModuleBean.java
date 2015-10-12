package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

/**
 * Content properties are one of the forms of persistence available for Confluence Connect add-ons, allowing you
 * to store JSON data and match it up with content.  Content Properties are stored as JSON objects and linked
 * to content, allowing you to track extra information that your add-on needs.  Values from these JSON objects
 * can be extracted and indexed and made available to CQL queries.
 *
 * Creating a complete content property comes in three parts.
 *
 * 1. Store and update your JSON data as a content property
 * <a href="https://developer.atlassian.com/display/CONFDEV/Content+Properties+in+the+REST+API">using the REST API</a>.
 * 1. Define the
 * <a href="../fragment/content-property-index-key-configuration.html">content property</a>, its
 * <a href="../fragment/content-property-index-extraction-configuration.html">extractions</a>, and optionally an
 * alias and <a href="../fragment/user-interface-support.html">UI support</a> in your add-on's descriptor.
 * See the <a href="../fragment/content-property-index-key-configuration.html">content property</a> documentation
 * for an example.
 * 1. Use <a href="https://developer.atlassian.com/display/CONFDEV/Advanced+Searching+using+CQL">CQL</a>
 * to query content based on your custom content property.
 *
 *#### Example
 *
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
