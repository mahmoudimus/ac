package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.WebPanelLayout;

public class WebPanelModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<WebPanelModuleBeanBuilder, WebPanelModuleBean>
{
    private String location;
    private WebPanelLayout layout;
    private String url;
    private Integer weight;

    public WebPanelModuleBeanBuilder()
    {

    }

    public WebPanelModuleBeanBuilder(WebPanelModuleBean webPanelBean)
    {
        super(webPanelBean);

        this.location = webPanelBean.getLocation();
        this.layout = webPanelBean.getLayout();
        this.url = webPanelBean.getUrl();
        this.weight = webPanelBean.getWeight();
    }


    public WebPanelModuleBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public WebPanelModuleBeanBuilder withLayout(WebPanelLayout layout){
        this.layout = layout;
        return this;
    }

    public WebPanelModuleBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }


    public WebPanelModuleBeanBuilder withWeight(int weight)
    {
        this.weight = weight;
        return this;
    }


    @Override
    public WebPanelModuleBean build()
    {
        return new WebPanelModuleBean(this);
    }
}
