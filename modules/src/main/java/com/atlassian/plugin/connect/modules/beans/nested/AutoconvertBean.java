package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.ArraySchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.AutoconvertBeanBuilder;
import com.google.common.collect.ImmutableList;

import java.util.List;


/**
 * Autoconvert allows your macro to be inserted into the editor when a recognised URL is pasted in by the user.
 * You define recognised URL patterns using 'matchers' which are registered in the editor when your add-on is installed.
 *
 * When the macro is created in the editor, the URL string that triggered the autoconvert will be captured and inserted
 * as a parameter on the macro body. You must define the name of this parameter by providing a string value for 'urlParameter'.
 * This allows you to access the URL that triggered the autoconvert.
 *
 *#### Example
 *
 * This example inserts a macro into the editor when a user pastes in certain simple Facebook links.
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#AUTOCONVERT_EXAMPLE}
 * @schemaTitle Autoconvert
 * @since 1.1.27
 */
@SchemaDefinition("autoconvert")
public class AutoconvertBean extends BaseModuleBean
{

    /**
     * The name of the macro parameter the matched url will be inserted into.
     */
    @Required
    private String urlParameter;

    /**
     * The list of patterns that define what URLs should be matched.
     */
    @ArraySchemaAttributes(maxItems = 200)
    private List<MatcherBean> matchers;

    public AutoconvertBean()
    {
        this.matchers = ImmutableList.of();
        this.urlParameter = "";
    }

    public AutoconvertBean(AutoconvertBeanBuilder builder)
    {
        super(builder);

        if (null == matchers)
        {
            this.matchers = ImmutableList.of();
        }

        if (null == urlParameter)
        {
            this.urlParameter = "";
        }
    }

    public static AutoconvertBeanBuilder newAutoconvertBean()
    {
        return new AutoconvertBeanBuilder();
    }

    public static AutoconvertBeanBuilder newAutoconvertBean(AutoconvertBean defaultBean)
    {
        return new AutoconvertBeanBuilder(defaultBean);
    }

    public List<MatcherBean> getMatchers()
    {
        return matchers;
    }

    public String getUrlParameter()
    {
        return urlParameter;
    }
}
