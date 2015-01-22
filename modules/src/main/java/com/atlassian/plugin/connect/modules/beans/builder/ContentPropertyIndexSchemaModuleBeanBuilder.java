package com.atlassian.plugin.connect.modules.beans.builder;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.ContentPropertyIndexSchemaModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

public class ContentPropertyIndexSchemaModuleBeanBuilder
        extends RequiredKeyBeanBuilder<ContentPropertyIndexSchemaModuleBeanBuilder, ContentPropertyIndexSchemaModuleBean>
{
    private final List<ContentPropertyIndexKeyConfigurationBean> keyConfigurations = Lists.newArrayList();

    @Override
    public ContentPropertyIndexSchemaModuleBean build()
    {
        return new ContentPropertyIndexSchemaModuleBean(this);
    }

    public ContentPropertyIndexSchemaModuleBeanBuilder withKeyConfiguration(ContentPropertyIndexKeyConfigurationBean keyConfiguration)
    {
        this.keyConfigurations.add(keyConfiguration);
        return this;
    }
}
