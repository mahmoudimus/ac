package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;

/**
 * A Confluence macro that loads the remote content as an IFrame.
 * Dynamic Content Macros render content on every page request and are suitable for add-ons that need to display content
 * that changes over time, or content that is specific to the authenticated user.
 *
 * JSON Example:
 * @exampleJson {@see ConnectJsonExamples#DYNAMIC_MACRO_EXAMPLE}
 * @schemaTitle Dynamic Content Macro
 * @since 1.0
 */
public class DynamicContentMacroModuleBean extends BaseContentMacroModuleBean
{
    public DynamicContentMacroModuleBean()
    {
    }

    public DynamicContentMacroModuleBean(DynamicContentMacroModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static DynamicContentMacroModuleBeanBuilder newDynamicContentMacroModuleBean()
    {
        return new DynamicContentMacroModuleBeanBuilder();
    }

    public static DynamicContentMacroModuleBeanBuilder newDynamicContentMacroModuleBean(DynamicContentMacroModuleBean defaultBean)
    {
        return new DynamicContentMacroModuleBeanBuilder(defaultBean);
    }
}
