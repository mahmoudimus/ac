package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.builder.ConfluenceThemeRouteInterceptionsBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * Defines where the blueprint template is located and the context for variable substitution.
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#BLUEPRINT_TEMPLATE_EXAMPLE}
 * @schemaTitle Confluence Theme Route Interceptions
 * @since 1.1.5
 */
public class ConfluenceThemeRouteInterceptionsBean
{
    private ConfluenceThemeRouteBean dashboard;
    private ConfluenceThemeRouteBean contentview;
    private ConfluenceThemeRouteBean spaceview;

    public ConfluenceThemeRouteBean getContentview()
    {
        return contentview;
    }

    public ConfluenceThemeRouteBean getDashboard()
    {
        return dashboard;
    }

    public ConfluenceThemeRouteBean getSpaceview()
    {
        return spaceview;
    }

    public ConfluenceThemeRouteInterceptionsBean(ConfluenceThemeRouteInterceptionsBeanBuilder builder)
    {
       copyFieldsByNameAndType(builder, this);
    }

    public static ConfluenceThemeRouteInterceptionsBeanBuilder newConfluenceThemeRouteInterceptionsBeanBuilder()
    {
       return new ConfluenceThemeRouteInterceptionsBeanBuilder();
    }
}
