package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.ConfluenceThemeRouteBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#BLUEPRINT_TEMPLATE_EXAMPLE}
 * @schemaTitle Confluence Theme Route
 * @since 1.1.5
 */
public class ConfluenceThemeRouteBean
{
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    public String getUrl()
    {
        return url;
    }

    public ConfluenceThemeRouteBean(ConfluenceThemeRouteBeanBuilder builder)
    {
       copyFieldsByNameAndType(builder, this);
    }

    public static ConfluenceThemeRouteBeanBuilder newConfluenceThemeRouteBeanBuilder()
    {
       return new ConfluenceThemeRouteBeanBuilder();
    }
}
