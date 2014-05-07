package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.StaticContentMacroModuleBeanBuilder;

/**
 * A Confluence macro that returns XHTML in the Confluence storage format. Note, unlike most
 * Connect modules, this content is not displayed in an iframe. Instead, your macro is responsible for returning valid
 * Storage Format XML to the confluence page, which Confluence will render for you at view time.
 *
 * Please consult [Confluence Storage Format](https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format)
 * for additional information about how to construct valid storage format XML.
 *
 *#### Use Caching
 *
 * Because any calls to the macro rendering service happen synchronously during page load, we strongly encourage the
 * implementations to take advantage of HTTP's caching mechanisms: Often, the rendered content only depends on the macro's
 * body and parameters. A good approach for this specific case is to prevent Connect from retrieving the content again, unless
 * the parameters or body have actually changed:
 *
 *     res.setHeader('Cache-Control', ['max-age=3600', 's-maxage=3600']);
 *
 * This response header tells the cache to use the response for an hour without asking the service again.
 * Because we declare the macro hash and parameters as URL variables, the URL will automatically change when the macro is changed.
 * This change will cause Connect to bypass the cache and to fetch the content from the add-on again.
 * So doing non-conditional caching works very well for this case. If the content of the macro varies with other data,
 * you could use `ETag` and `If-None-Match` to render the macro conditionally.
 *
 * Also keep in mind that the calls are made from the Confluence server to the add-on host, and the cache is shared
 * between users. If you need to prevent any caching on the server, use
 *
 *     Cache-Control: no-cache
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#STATIC_MACRO_EXAMPLE}
 * @schemaTitle Static Content Macro
 * @since 1.0
 */
@SchemaDefinition("staticContentMacro")
public class StaticContentMacroModuleBean extends BaseContentMacroModuleBean
{
    public StaticContentMacroModuleBean()
    {
    }

    public StaticContentMacroModuleBean(StaticContentMacroModuleBeanBuilder builder)
    {
        super(builder);
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
