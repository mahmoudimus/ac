package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.UISupportModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexFieldType;
import com.atlassian.plugin.connect.modules.beans.nested.ContentPropertyIndexKeyConfigurationBean;
import com.google.common.collect.Lists;

import java.util.List;

public class ContentPropertyIndexExtractionConfigurationBeanBuilder
        implements ModuleBeanBuilder<ContentPropertyIndexExtractionConfigurationBean>
{
    private final List<ContentPropertyIndexKeyConfigurationBean> keyConfigurations = Lists.newArrayList();
    private String objectName;
    private ContentPropertyIndexFieldType type;
    private String alias;
    private UISupportModuleBean uiSupport;


    @Override
    public ContentPropertyIndexExtractionConfigurationBean build()
    {
        return new ContentPropertyIndexExtractionConfigurationBean(this);
    }

    public ContentPropertyIndexExtractionConfigurationBeanBuilder withObjectName(String objectName)
    {
        this.objectName = objectName;
        return this;
    }

    public ContentPropertyIndexExtractionConfigurationBeanBuilder withType(ContentPropertyIndexFieldType type)
    {
        this.type = type;
        return this;
    }

    public ContentPropertyIndexExtractionConfigurationBeanBuilder withAlias(String alias)
    {
        this.alias = alias;
        return this;
    }

    public ContentPropertyIndexExtractionConfigurationBeanBuilder withUiSupport(UISupportModuleBean uiSupport)
    {
        this.uiSupport = uiSupport;
        return this;
    }

    public List<ContentPropertyIndexKeyConfigurationBean> getKeyConfigurations()
    {
        return keyConfigurations;
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
