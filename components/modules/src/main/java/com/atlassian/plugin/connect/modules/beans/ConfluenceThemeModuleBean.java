package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.ConfluenceThemeModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.ConfluenceThemeRouteInterceptionsBean;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 *
 * @since 1.1.62
 */
public class ConfluenceThemeModuleBean extends RequiredKeyBean
{
    @Required
    private ConfluenceThemeRouteInterceptionsBean routes;

    private IconBean icon;

    public ConfluenceThemeModuleBean(ConfluenceThemeModuleBeanBuilder builder)
    {
        super(builder);
        copyFieldsByNameAndType(builder, this);
    }

    public static ConfluenceThemeModuleBeanBuilder newConfluenceThemeModuleBean()
    {
        return new ConfluenceThemeModuleBeanBuilder();
    }

    public ConfluenceThemeRouteInterceptionsBean getRoutes()
    {
        return routes;
    }

    public IconBean getIcon()
    {
        return icon;
    }
}
