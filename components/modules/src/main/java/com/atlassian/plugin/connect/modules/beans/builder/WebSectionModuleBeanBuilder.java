package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class WebSectionModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<WebSectionModuleBeanBuilder, WebSectionModuleBean> {
    private String location;
    private Integer weight;
    private I18nProperty tooltip;

    public WebSectionModuleBeanBuilder() {

    }

    public WebSectionModuleBeanBuilder(WebSectionModuleBean defaultBean) {
        super(defaultBean);

        this.location = defaultBean.getLocation();
        this.weight = defaultBean.getWeight();
        this.tooltip = defaultBean.getTooltip();
    }

    public WebSectionModuleBeanBuilder withLocation(String location) {
        this.location = location;
        return this;
    }

    public WebSectionModuleBeanBuilder withWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public WebSectionModuleBeanBuilder withTooltip(I18nProperty tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public WebSectionModuleBean build() {
        return new WebSectionModuleBean(this);
    }
}
