package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

public class DashboardItemModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<DashboardItemModuleBeanBuilder, DashboardItemModuleBean>
{
    private I18nProperty title;
    private I18nProperty description;
    private String url;
    private IconBean icon;

    public DashboardItemModuleBeanBuilder()
    {
        this.title = I18nProperty.empty();
        this.description = I18nProperty.empty();
        this.icon = null;
    }

    public DashboardItemModuleBeanBuilder(DashboardItemModuleBean dashboardItemModuleBean)
    {
        super(dashboardItemModuleBean);
        this.title = dashboardItemModuleBean.getTitle();
        this.description = dashboardItemModuleBean.getDescription();
        this.url = dashboardItemModuleBean.getUrl();
        this.icon = dashboardItemModuleBean.getIcon();
    }

    public DashboardItemModuleBeanBuilder withTitle(I18nProperty title)
    {
        this.title = title;
        return this;
    }

    public DashboardItemModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public DashboardItemModuleBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public DashboardItemModuleBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    @Override
    public DashboardItemModuleBean build()
    {
        return new DashboardItemModuleBean(this);
    }

}
