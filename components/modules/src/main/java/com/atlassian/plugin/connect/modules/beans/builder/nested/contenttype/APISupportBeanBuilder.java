package com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype;

import java.util.Set;

import com.atlassian.plugin.connect.modules.beans.nested.contenttype.BodyType;
import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.APISupportBean;

public class APISupportBeanBuilder
        extends BaseModuleBeanBuilder<APISupportBeanBuilder, APISupportBean>
{
    private BodyType bodyType;
    private Set<String> supportedContainerTypes;
    private Set<String> supportedContainedTypes;
    private String onCreateUrl;
    private String onUpdateUrl;
    private String onDeleteUrl;

    public APISupportBeanBuilder()
    {
    }

    public APISupportBeanBuilder withBodyType(String bodyType)
    {
        this.bodyType = BodyType.valueOf(bodyType);
        return this;
    }

    public APISupportBeanBuilder withSupportedContainerTypes(Set<String> supportedContainerTypes)
    {
        this.supportedContainerTypes = supportedContainerTypes;
        return this;
    }

    public APISupportBeanBuilder withSupportedContainedTypes(Set<String> supportedChildrenTypes)
    {
        this.supportedContainedTypes = supportedChildrenTypes;
        return this;
    }

    public APISupportBeanBuilder withOnCreateUrl(String onCreateUrl)
    {
        this.onCreateUrl = onCreateUrl;
        return this;
    }

    public APISupportBeanBuilder withOnUpdateUrl(String onUpdateUrl)
    {
        this.onUpdateUrl = onUpdateUrl;
        return this;
    }

    public APISupportBeanBuilder withOnDeleteUrl(String onDeleteUrl)
    {
        this.onDeleteUrl = onDeleteUrl;
        return this;
    }

    public APISupportBean build()
    {
        return new APISupportBean(this);
    }
}
