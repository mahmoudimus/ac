package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

/**
 * Blueprints allow your connect add on to provide content creation templates.
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#BLUEPRINT_EXAMPLE}
 * @schemaTitle Blueprints
 * @since 1.1.9
 */
public class BlueprintModuleBean extends RequiredKeyBean
{

    private IconBean icon;

    @Required
    private BlueprintTemplateBean template;

    @StringSchemaAttributes(format="createResult")
    private String createResult;

    public BlueprintModuleBean() { }

    public BlueprintModuleBean(BlueprintModuleBeanBuilder builder)
    {
        super(builder);
    }

    public static BlueprintModuleBeanBuilder newBlueprintModuleBean()
    {
        return new BlueprintModuleBeanBuilder();
    }

    public BlueprintTemplateBean getBlueprintTemplate()
    {
        return template;
    }

    public String getCreateResult()
    {
        return createResult;
    }

    public IconBean getIcon()
    {
        return icon;
    }
}
