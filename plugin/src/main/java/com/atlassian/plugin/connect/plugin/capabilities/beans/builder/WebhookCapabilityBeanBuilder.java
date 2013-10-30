package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebhookCapabilityBean;

public class WebhookCapabilityBeanBuilder extends BeanWithParamsBuilder<WebhookCapabilityBeanBuilder, WebhookCapabilityBean>
{
    private String event;
    private String url;

    public WebhookCapabilityBeanBuilder()
    {
    }

    public WebhookCapabilityBeanBuilder(WebhookCapabilityBean defaultBean)
    {
        this.event = defaultBean.getEvent();
        this.url = defaultBean.getUrl();
    }

    public WebhookCapabilityBeanBuilder withEvent(String event)
    {
        this.event = event;
        return this;
    }

    public WebhookCapabilityBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public WebhookCapabilityBean build()
    {
        return new WebhookCapabilityBean(this);
    }

}
