package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

import java.util.Map;

public class ConnectTabPanelModuleBeanBuilder<T extends ConnectTabPanelModuleBeanBuilder, B extends ConnectTabPanelModuleBean> extends BeanWithKeyParamsAndConditionsBuilder<T, B> {
    private String url;
    private Integer weight;

    public ConnectTabPanelModuleBeanBuilder(ConnectTabPanelModuleBean defaultBean) {
        super(defaultBean);
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
    }

    public ConnectTabPanelModuleBeanBuilder() {
    }

    public T withUrl(String url) {
        this.url = url;
        return (T) this;
    }

    public T withWeight(int weight) {
        this.weight = weight;
        return (T) this;
    }

    public String getUrl() {
        return url;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public T withConditions(ConditionalBean... beans) {
        super.withConditions(beans);
        return (T) this;
    }

    @Override
    public T withParams(Map<String, String> params) {
        super.withParams(params);
        return (T) this;
    }

    @Override
    public T withParam(String key, String value) {
        super.withParam(key, value);
        return (T) this;
    }

    @Override
    public T withKey(String key) {
        super.withKey(key);
        return (T) this;
    }

    @Override
    public T withName(I18nProperty name) {
        super.withName(name);
        return (T) this;
    }

    @Override
    public B build() {
        return (B) new ConnectTabPanelModuleBean(this);
    }
}
