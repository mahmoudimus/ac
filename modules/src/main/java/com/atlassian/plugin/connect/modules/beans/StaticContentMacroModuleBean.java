package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.MacroHttpMethod;

/**
 * A Confluence macro that returns XHTML in the Confluence storage format. The add-on will only be called on creation of
 * the macro or when the macro is edited, but not on page view. Instead, your macro is responsible for adding valid
 * Storage Format XML to the confluence page, which the Confluence will render for you at view time. Note, unlike most
 * Connect modules, this content is not displayed in an iframe.
 * <p/>
 * Please consult [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
 * for additional information about how to construct valid storage format XML.
 * <p/>
 * #### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#STATIC_MACRO_EXAMPLE}
 * @schemaTitle Static Content Macro
 * @since 1.0
 */
public class StaticContentMacroModuleBean extends BaseContentMacroModuleBean
{

    /**
     * The HTTP method to use when calling the macro. The default value is GET.
     */
    @CommonSchemaAttributes(defaultValue = "get")
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
