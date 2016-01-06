package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * Defines where the blueprint template is located and the context for variable substitution.
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#BLUEPRINT_TEMPLATE_EXAMPLE}
 * @schemaTitle Blueprint Template
 * @since 1.1.5
 */
public class BlueprintTemplateBean
{

    /**
     * The URL of the add-on resource that provides the blueprint template content. This URL has to be relative
     * to the add-on base URL.
     */
    @Required
    @StringSchemaAttributes(format="uri")
    private String url;

    /**
     * Defines add-on server resource that provides JSON object used for substitute variables defined in template.<br>
     * For more about how to define variables in blueprint template and template context please follow the example in
     * <a href="/modules/fragment/blueprint-template-context.html">BlueprintTemplateContextBean</a>
     */
    @Required
    private BlueprintTemplateContextBean blueprintContext;

    public static BlueprintTemplateBeanBuilder newBlueprintTemplateBeanBuilder()
    {
        return new BlueprintTemplateBeanBuilder();
    }

    public BlueprintTemplateBean(BlueprintTemplateBeanBuilder builder)
    {
        copyFieldsByNameAndType(builder, this);
    }

    public String getUrl()
    {
        return url;
    }

    public BlueprintTemplateContextBean getBlueprintContext()
    {
        return blueprintContext;
    }
}
