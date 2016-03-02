package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.SpaceToolsTabModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

import java.util.Map;

public class SpaceToolsTabModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<SpaceToolsTabModuleBeanBuilder, SpaceToolsTabModuleBean> {
    private String url;
    private Integer weight;
    private String location;

    public SpaceToolsTabModuleBeanBuilder(SpaceToolsTabModuleBean defaultBean) {
        super(defaultBean);
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
        this.location = defaultBean.getLocation();
    }

    public SpaceToolsTabModuleBeanBuilder() {
    }

    public SpaceToolsTabModuleBeanBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public SpaceToolsTabModuleBeanBuilder withWeight(int weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public SpaceToolsTabModuleBeanBuilder withConditions(ConditionalBean... beans) {
        super.withConditions(beans);
        return this;
    }

    @Override
    public SpaceToolsTabModuleBeanBuilder withParams(Map<String, String> params) {
        super.withParams(params);
        return this;
    }

    @Override
    public SpaceToolsTabModuleBeanBuilder withParam(String key, String value) {
        super.withParam(key, value);
        return this;
    }

    @Override
    public SpaceToolsTabModuleBeanBuilder withKey(String key) {
        super.withKey(key);
        return this;
    }

    @Override
    public SpaceToolsTabModuleBeanBuilder withName(I18nProperty name) {
        super.withName(name);
        return this;
    }

    public SpaceToolsTabModuleBeanBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public SpaceToolsTabModuleBean build() {
        return new SpaceToolsTabModuleBean(this);
    }

}
