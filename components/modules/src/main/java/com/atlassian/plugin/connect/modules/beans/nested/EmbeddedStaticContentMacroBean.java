package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.EmbeddedStaticContentMacroBeanBuilder;

/**
 * An embedded reference to a static macro resource.  These macro definitions are a subset of
 * a Static Content Macro, defining only what cannot be inferred from the containing dynamic macro.
 *
 * These embedded static content macros are used to define render mode mappings for your dynamic content macro.
 *
 *#### Example
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#EMBEDDED_STATIC_MACRO_EXAMPLE}
 * @schemaTitle Embedded Static Content Macro
 * @since 1.0
 */
@SchemaDefinition("embeddedStaticContentMacroBean")
public class EmbeddedStaticContentMacroBean extends BaseModuleBean
{
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    public EmbeddedStaticContentMacroBean(EmbeddedStaticContentMacroBeanBuilder builder)
    {
        super(builder);
    }

    public String getUrl()
    {
        return url;
    }

    public static EmbeddedStaticContentMacroBeanBuilder newEmbeddedStaticContentMacroModuleBean()
    {
        return new EmbeddedStaticContentMacroBeanBuilder();
    }
}
