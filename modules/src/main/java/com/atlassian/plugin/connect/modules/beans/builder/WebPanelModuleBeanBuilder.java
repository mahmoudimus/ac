package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.WebPanelLayout;

public class WebPanelModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<WebPanelModuleBeanBuilder, WebPanelModuleBean>
{
    private String location;
    private WebPanelLayout layout;
    private String url;
    private Integer weight;
    private I18nProperty tooltip;
    private boolean noPadding;

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
        this.tooltip = webPanelBean.getTooltip();
        this.noPadding = webPanelBean.isWithoutPadding();
    }


    public WebPanelModuleBeanBuilder withLocation(String location)
    {
        this.location = location;
        return this;
    }

    public WebPanelModuleBeanBuilder withLayout(WebPanelLayout layout)
    {
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

    public WebPanelModuleBeanBuilder withTooltip(I18nProperty tooltip)
    {
        this.tooltip = tooltip;
        return this;
    }

    public WebPanelModuleBeanBuilder withoutPadding(boolean withoutPadding)
    {
        this.noPadding = withoutPadding;
        return this;
    }

    @Override
    public WebPanelModuleBean build()
    {
        return new WebPanelModuleBean(this);
    }
}
