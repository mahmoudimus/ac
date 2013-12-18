package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroHttpMethod;

/**
 * A Confluence macro that returns XHTML in the Confluence storage format.
 * The add-on will only be called on creation of the macro or when the macro is edited,
 * but not on page view.
 *
 * Please consult [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
 * for additional information.
 *
 * @exampleJson example: {@see com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectJsonExamples#STATIC_MACRO_EXAMPLE}
 * @schemaTitle Static Content Macro
 * @since 1.0
 */
public class StaticContentMacroModuleBean extends BaseContentMacroModuleBean
{

    /**
     * The HTTP method to use when calling the macro. The default value is GET.
     */
    private MacroHttpMethod method;

    public StaticContentMacroModuleBean()
    {
        init();
    }

    public StaticContentMacroModuleBean(StaticContentMacroModuleBeanBuilder builder)
    {
        super(builder);
        init();
    }

    private void init()
    {
        if (null == method)
        {
            method = MacroHttpMethod.GET;
        }
    }

    public MacroHttpMethod getMethod()
    {
        return method;
    }

    public static StaticContentMacroModuleBeanBuilder newStaticContentMacroModuleBean()
    {
        return new StaticContentMacroModuleBeanBuilder();
    }

    public static StaticContentMacroModuleBeanBuilder newStaticContentMacroModuleBean(StaticContentMacroModuleBean defaultBean)
    {
        return new StaticContentMacroModuleBeanBuilder(defaultBean);
    }
}
