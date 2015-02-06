package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyIndexSchemaModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

/**
 * Content properties are one of the forms of persistence available for add-on developers,
 * a key-value storage associated with a piece of Confluence content.
 * These values are indexed by Confluence and able to be queried using CQL. For more information,
 * please see the [Confluence documentation on content properties](https://developer.atlassian.com/display/CONFDEV/Content+Properties+in+the+REST+API).
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTENT_PROPERTY_EXAMPLE}
 * @schemaTitle Content Property
 * @since 1.0
 */
public class ContentPropertyIndexSchemaModuleBean extends RequiredKeyBean
{
    /**
     * List of properties from which selected values are indexed.
     */
    private final List<ContentPropertyIndexKeyConfigurationBean> keyConfigurations = Lists.newArrayList();

    public ContentPropertyIndexSchemaModuleBean()
    {
    }

    public ContentPropertyIndexSchemaModuleBean(ContentPropertyIndexSchemaModuleBeanBuilder contentPropertyIndexSchemaModuleBeanBuilder)
    {
        super(contentPropertyIndexSchemaModuleBeanBuilder);
    }

    public List<ContentPropertyIndexKeyConfigurationBean> getKeyConfigurations()
    {
        return keyConfigurations;
    }

    public static ContentPropertyIndexSchemaModuleBeanBuilder newContentPropertyIndexSchemaModuleBean()
    {
        return new ContentPropertyIndexSchemaModuleBeanBuilder();
    }
}
