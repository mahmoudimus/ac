package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class DashboardItemModuleBeanBuilder extends BeanWithKeyParamsAndConditionsBuilder<DashboardItemModuleBeanBuilder, DashboardItemModuleBean>
{
    private Boolean configurable;
    private I18nProperty description;
    private String url;
    private String thumbnailUrl;

    public DashboardItemModuleBeanBuilder()
    {
        this.description = I18nProperty.empty();
        this.thumbnailUrl = null;
    }

    public DashboardItemModuleBeanBuilder(DashboardItemModuleBean dashboardItemModuleBean)
    {
        super(dashboardItemModuleBean);
        this.description = dashboardItemModuleBean.getDescription();
        this.url = dashboardItemModuleBean.getUrl();
        this.thumbnailUrl = dashboardItemModuleBean.getThumbnailUrl();
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

    public DashboardItemModuleBeanBuilder withThumbnailUrl(String thumbnailUrl)
    {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public DashboardItemModuleBeanBuilder configurable(Boolean configurable)
    {
        this.configurable = configurable;
        return this;
    }

    @Override
    public DashboardItemModuleBean build()
    {
        return new DashboardItemModuleBean(this);
    }

}
