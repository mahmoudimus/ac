package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.EntityPropertyIndexDocumentModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyType;
import com.google.common.collect.Lists;

import java.util.List;

public class EntityPropertyIndexDocumentModuleBeanBuilder extends NameToKeyBeanBuilder<EntityPropertyIndexDocumentModuleBeanBuilder, EntityPropertyIndexDocumentModuleBean>
{
    private List<EntityPropertyIndexKeyConfigurationBean> keyConfigurations;
    private EntityPropertyType entityPropertyType;

    public EntityPropertyIndexDocumentModuleBeanBuilder()
    {
        this.keyConfigurations = Lists.newArrayList();
        this.entityPropertyType = EntityPropertyType.issue;
    }

    public EntityPropertyIndexDocumentModuleBeanBuilder(EntityPropertyIndexDocumentModuleBean defaultBean)
    {
        this.keyConfigurations = defaultBean.getKeyConfigurations();
        this.entityPropertyType = defaultBean.getPropertyType();
    }

    public EntityPropertyIndexDocumentModuleBeanBuilder withKeyConfiguration(EntityPropertyIndexKeyConfigurationBean keyConfiguration)
    {
        this.keyConfigurations.add(keyConfiguration);
        return this;
    }

    public EntityPropertyIndexDocumentModuleBeanBuilder withPropertyType(EntityPropertyType propertyType)
    {
        this.entityPropertyType = propertyType;
        return this;
    }

    @Override
    public EntityPropertyIndexDocumentModuleBean build()
    {
        return new EntityPropertyIndexDocumentModuleBean(this);
    }
}
