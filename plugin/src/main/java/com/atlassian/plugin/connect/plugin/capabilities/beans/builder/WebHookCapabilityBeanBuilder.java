package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebHookCapabilityBean;

public class WebHookCapabilityBeanBuilder extends BeanWithParamsBuilder<WebHookCapabilityBeanBuilder, WebHookCapabilityBean>
{
    private String event;
    private String url;

    public WebHookCapabilityBeanBuilder()
    {
    }

    public WebHookCapabilityBeanBuilder(WebHookCapabilityBean defaultBean)
    {
        this.event = defaultBean.getEvent();
        this.url = defaultBean.getUrl();
    }

    public WebHookCapabilityBeanBuilder withEvent(String event)
    {
        this.event = event;
        return this;
    }

    public WebHookCapabilityBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public WebHookCapabilityBean build()
    {
        return new WebHookCapabilityBean(this);
    }

}
