package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;

public class WebHookModuleBeanBuilder extends BeanWithParamsBuilder<WebHookModuleBeanBuilder, WebHookModuleBean>
{
    private String event;
    private String url;

    public WebHookModuleBeanBuilder()
    {
    }

    public WebHookModuleBeanBuilder(WebHookModuleBean defaultBean)
    {
        this.event = defaultBean.getEvent();
        this.url = defaultBean.getUrl();
    }

    public WebHookModuleBeanBuilder withEvent(String event)
    {
        this.event = event;
        return this;
    }

    public WebHookModuleBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    @Override
    public WebHookModuleBean build()
    {
        return new WebHookModuleBean(this);
    }

}
