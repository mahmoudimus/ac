package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

import java.util.Map;

@SuppressWarnings("unchecked")
public class ConnectPageModuleBeanBuilder<T extends ConnectPageModuleBeanBuilder, B extends ConnectPageModuleBean> extends BeanWithKeyParamsAndConditionsBuilder<T, B> {
    private String url;
    private Integer weight;
    private String location;
    private IconBean icon;

    public ConnectPageModuleBeanBuilder(ConnectPageModuleBean defaultBean) {
        super(defaultBean);
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
        this.location = defaultBean.getLocation();
        this.icon = defaultBean.getIcon();
    }

    public ConnectPageModuleBeanBuilder() {
    }

    public T withUrl(String url) {
        this.url = url;
        return (T) this;
    }

    public T withWeight(int weight) {
        this.weight = weight;
        return (T) this;
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

    public T withLocation(String location) {
        this.location = location;
        return (T) this;
    }

    public T withIcon(IconBean icon) {
        this.icon = icon;
        return (T) this;
    }

    @Override
    public B build() {
        return (B) new ConnectPageModuleBean(this);
    }

}
