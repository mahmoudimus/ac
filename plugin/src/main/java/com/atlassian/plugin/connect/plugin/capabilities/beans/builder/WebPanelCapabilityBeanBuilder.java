package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelCapabilityBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelLayout;

public class WebPanelCapabilityBeanBuilder extends NameToKeyBeanBuilder<WebPanelCapabilityBeanBuilder, WebPanelCapabilityBean>
{
    private String location;
    private WebPanelLayout layout;
    private String url;
    private Integer weight;

    public WebPanelCapabilityBeanBuilder()
    {

    }

    public WebPanelCapabilityBeanBuilder(WebPanelCapabilityBean webPanelBean)
    {
        super(webPanelBean);

        this.location = webPanelBean.getLocation();
        this.layout = webPanelBean.getLayout();
        this.url = webPanelBean.getUrl();
        this.weight = webPanelBean.getWeight();
    }


    public WebPanelCapabilityBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withLayout(WebPanelLayout layout){
        this.layout = layout;
        return this;
    }

    public WebPanelCapabilityBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }


    public WebPanelCapabilityBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }


    @Override
    public WebPanelCapabilityBean build()
    {
        return new WebPanelCapabilityBean(this);
    }
}
