package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateContextBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * Defines the context of the blueprint.
 *
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_TEMPLATE_CONTEXT_EXAMPLE}
 * @schemaTitle Blueprint Template Context
 */
public class BlueprintTemplateContextBean
{
    /**
     * <p>A URL to which a POST request will be made during the rendering of the blueprint for which this context bean is
     * associated. The expected return value is a JSON array of context values, e.g.:</p>
     *
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
     *
     * <p>The <tt>identifier</tt> must be unique
     * The <tt>representation</tt> must be one of : "plain", "wiki", or "storage". If unset, it defaults to "plain"
     * The <tt>value</tt> must be a string that conforms to the <tt>representation</tt>. "plain" is plain text, "wiki" is wiki markup, and "storage" is
     * valid confluence xhtml storage format as documented on
     * <a href="https://confluence.atlassian.com/display/DOC/Confluence+Storage+Format">Confluence Storage Format</a></p>
     *
     * <h3>Example JSON</h3>
     *
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_POST_BODY_EXAMPLE}
     */
    @Required
    @StringSchemaAttributes (format = "uri")
    private String url;

    public BlueprintTemplateContextBean(BlueprintTemplateContextBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }

    public static BlueprintTemplateContextBeanBuilder newBlueprintTemplateContextBeanBuilder()
    {
        return new BlueprintTemplateContextBeanBuilder();
    }

    public String getUrl()
    {
        return url;
    }
}
