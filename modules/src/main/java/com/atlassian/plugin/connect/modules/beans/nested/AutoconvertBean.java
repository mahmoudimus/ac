package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.AutoconvertBeanBuilder;
import com.google.common.collect.ImmutableList;

import java.util.List;


/**
 * Autoconvert allows the dynamic insertion of your macro into the editor, triggered by the user pasting a recognised
 * URL. You define a set of 'matchers' for what the URLs to act on will look like, and these are registered in the
 * editor when your add-on is installed.
 *
 *#### Example
 *
 * The following example would insert a macro into the editor when a user pastes in certain simple Facebook links.
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#AUTOCONVERT_EXAMPLE}
 * @schemaTitle Autoconvert
 * @since 1.1
 */
@SchemaDefinition("autoconvert")
public class AutoconvertBean extends BaseModuleBean {

    /**
     * The name of the macro parameter the matched url will be inserted into.
     */
    @Required
    private String urlParameter;

    /**
     * The list of patterns that define what URLs should be matched.
     */
    private List<MatcherBean> matchers;

    public AutoconvertBean() {
        this.matchers = ImmutableList.of();
        this.urlParameter = "";
    }

    public AutoconvertBean(AutoconvertBeanBuilder builder) {
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

    public List<MatcherBean> getMatchers() {
        return matchers;
    }

    public String getUrlParameter()
    {
        return urlParameter;
    }
}
