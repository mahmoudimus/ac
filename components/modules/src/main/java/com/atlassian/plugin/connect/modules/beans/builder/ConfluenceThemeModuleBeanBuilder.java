package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean;

/**
 *
 */
public class ConfluenceThemeModuleBeanBuilder extends RequiredKeyBeanBuilder<ConfluenceThemeModuleBeanBuilder, ConfluenceThemeModuleBean>
{
    private ConfluenceThemeRouteInterceptionsBean routes;
    private IconBean icon;

    public ConfluenceThemeModuleBeanBuilder withRoutes(ConfluenceThemeRouteInterceptionsBean routes)
    {
        this.routes = routes;
        return this;
    }

    public ConfluenceThemeModuleBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    public ConfluenceThemeModuleBean build()
    {
        return new ConfluenceThemeModuleBean(this);
    }
}
