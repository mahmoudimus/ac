package com.atlassian.plugin.connect.modules.beans;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.builder.ContentPropertyIndexSchemaModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

/**
 * @since 1.1.20
 */
public class ContentPropertyIndexSchemaModuleBean extends RequiredKeyBean
{
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
