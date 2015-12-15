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
 * @schemaTitle Remote Blueprint Template Context
 */
public class BlueprintTemplateContextBean
{
    /**
     * A URL which confluence will make a POST request to, during the rendering of the blueprint for which this context bean is
     * associated. The expected return value is a JSON object with string keys, and values of :
     *
     * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_CONTEXT_RESPONSE_EXAMPLE}
     *
     * #### Example JSON
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
