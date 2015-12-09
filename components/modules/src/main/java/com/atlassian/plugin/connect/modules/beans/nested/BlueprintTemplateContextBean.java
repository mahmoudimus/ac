package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateContextBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * @exampleJson {@link ConnectJsonExamples#BLUEPRINT_TEMPLATE_CONTEXT_EXAMPLE}
 * @schemaTitle Remote Blueprint Template Context
 */
public class BlueprintTemplateContextBean
{
    /**
     * The URL of the add-on server resource that provides the blueprint template with the blueprintContext variables required for rendering. This URL
     * will be POST'ed to during the creation of a blueprint with some data, and the expected return value is a JSON object whose keys are the
     * variables found in the template, and whose values are the values to be used in the substitution.
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
