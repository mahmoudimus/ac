package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConditionalBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectTabPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;

public class ConnectTabPanelCapabilityBeanBuilder<T extends ConnectTabPanelCapabilityBeanBuilder, B extends ConnectTabPanelCapabilityBean> extends BeanWithKeyParamsAndConditionsBuilder<T, B>
{
    private String url;
    private Integer weight;

    public ConnectTabPanelCapabilityBeanBuilder(ConnectTabPanelCapabilityBean defaultBean)
    {
        super(defaultBean);
        this.weight = defaultBean.getWeight();
        this.url = defaultBean.getUrl();
    }

    public ConnectTabPanelCapabilityBeanBuilder()
    {
    }

    public T withUrl(String url)
    {
        this.url = url;
        return (T) this;
    }

    public T withWeight(int weight)
    {
        this.weight = weight;
        return (T) this;
    }

    public String getUrl()
    {
        return url;
    }

    public int getWeight()
    {
        return weight;
    }

    @Override
    public T withConditions(ConditionalBean... beans)
    {
        super.withConditions(beans);
        return (T) this;
    }

    @Override
    public T withParams(Map<String, String> params)
    {
        super.withParams(params);
        return (T) this;
    }

    @Override
    public T withParam(String key, String value)
    {
        super.withParam(key, value);
        return (T) this;
    }

    @Override
    public T withKey(String key)
    {
        super.withKey(key);
        return (T) this;
    }

    @Override
    public T withName(I18nProperty name)
    {
        super.withName(name);
        return (T) this;
    }

    @Override
    public B build()
    {
        return (B) new ConnectTabPanelCapabilityBean(this);
    }
}
