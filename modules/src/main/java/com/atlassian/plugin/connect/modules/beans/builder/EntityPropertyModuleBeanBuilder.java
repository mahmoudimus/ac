package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyType;
import com.google.common.collect.Lists;

import java.util.List;

public class EntityPropertyModuleBeanBuilder extends RequiredKeyBeanBuilder<EntityPropertyModuleBeanBuilder, EntityPropertyModuleBean>
{
    private List<EntityPropertyIndexKeyConfigurationBean> keyConfigurations;
    private EntityPropertyType entityPropertyType;

    public EntityPropertyModuleBeanBuilder()
    {
        this.keyConfigurations = Lists.newArrayList();
        this.entityPropertyType = EntityPropertyType.issue;
    }

    public EntityPropertyModuleBeanBuilder(EntityPropertyModuleBean defaultBean)
    {
        this.keyConfigurations = defaultBean.getKeyConfigurations();
        this.entityPropertyType = defaultBean.getEntityType();
    }

    public EntityPropertyModuleBeanBuilder withKeyConfiguration(EntityPropertyIndexKeyConfigurationBean keyConfiguration)
    {
        this.keyConfigurations.add(keyConfiguration);
        return this;
    }

    public EntityPropertyModuleBeanBuilder withEntityType(EntityPropertyType propertyType)
    {
        this.entityPropertyType = propertyType;
        return this;
    }

    @Override
    public EntityPropertyModuleBean build()
    {
        return new EntityPropertyModuleBean(this);
    }
}
