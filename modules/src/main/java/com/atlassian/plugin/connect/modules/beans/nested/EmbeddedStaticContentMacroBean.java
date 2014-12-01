package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.EmbeddedStaticContentMacroBeanBuilder;

/**
 * An embedded reference to a static macro resource.  These embedded versions of static macros are used to define
 * a static macro render mode for a dynamic macro.
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#EMBEDDED_STATIC_MACRO_EXAMPLE}
 * @schemaTitle Dynamic Content Macro
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
