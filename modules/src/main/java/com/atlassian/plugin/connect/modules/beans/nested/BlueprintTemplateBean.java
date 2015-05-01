package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintTemplateBeanBuilder;

import static com.atlassian.plugin.connect.modules.util.ConnectReflectionHelper.copyFieldsByNameAndType;

/**
 * Defines where the blueprint template is located.
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#BLUEPRINT_TEMPLATE_EXAMPLE}
 * @schemaTitle Remote Blueprint Template
 * @since 1.1.5
 */
public class BlueprintTemplateBean {

    /**
     * The URL of the add-on resource that provides the blueprint template content. This URL has to be relative
     * to the add-on base URL.
     */
    @Required
    @StringSchemaAttributes(format="url-template")
    private String url;

    public static BlueprintTemplateBeanBuilder newBlueprintTemplateBeanBuilder() {
        return new BlueprintTemplateBeanBuilder();
    }

    public BlueprintTemplateBean(BlueprintTemplateBeanBuilder builder) {
        copyFieldsByNameAndType(builder, this);
    }

    public String getUrl() {
        return url;
    }

}
