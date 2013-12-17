package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty.empty;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean.newLinkBean;

/**
 * A Confluence macro that loads the remote content as an IFrame.
 * Dynamic Content Macros render content on every page request and are suitable for add-ons that need to display content
 * that changes over time, or content that is specific to the authenticated user.
 *
 * Json Example:
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
