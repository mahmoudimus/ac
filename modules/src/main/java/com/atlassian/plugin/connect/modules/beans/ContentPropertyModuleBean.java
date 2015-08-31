package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

/**
 * Content properties are one of the forms of persistence available for connect, allowing your
 * addon to store json data and match it up with Confluence content.
 *
 * Values from the stored json object can be extracted and indexed and made available to CQL queries.
 *
 * Creating a complete content property comes in three parts.
 *
 * 1. Store and update your json data as a content property
 * [using the REST API](https://developer.atlassian.com/display/CONFDEV/Content+Properties+in+the+REST+API).
 * 1. Define the [content property key](../fragment/content-property-index-key-configuration.html),
 * its [extractions](../fragment/content-property-index-extraction-configuration.html), and optionally an
 * alias and [UI support](../fragment/ui-support.html) in your add-on's descriptor.
 * See the [content property key](../fragment/content-property-index-key-configuration.html) documentation
 * for an example.
 * 1. Use CQL to query content based on your custom content property.
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
     * List of properties from which selected values are indexed.
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
