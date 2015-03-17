package com.atlassian.plugin.connect.modules.beans.builder;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.ContentPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;

import com.google.common.collect.Lists;

public class ContentPropertyModuleBeanBuilder
        extends RequiredKeyBeanBuilder<ContentPropertyModuleBeanBuilder, ContentPropertyModuleBean>
{
    private final List<ContentPropertyIndexKeyConfigurationBean> keyConfigurations = Lists.newArrayList();

    @Override
    public ContentPropertyModuleBean build()
    {
        return new ContentPropertyModuleBean(this);
    }

    public ContentPropertyModuleBeanBuilder withKeyConfiguration(ContentPropertyIndexKeyConfigurationBean keyConfiguration)
    {
        this.keyConfigurations.add(keyConfiguration);
        return this;
    }
}
