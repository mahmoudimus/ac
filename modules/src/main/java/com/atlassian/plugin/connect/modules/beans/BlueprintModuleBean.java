package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.CreateResultType;
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

    /**
     * Defines the screen to go to when creating this type of Blueprint. A value of `view` causes Confluence to bypass the
     * editor page and automatically create the page content. The user lands in the view of the created page. When `edit`,
     * the user is sent to the editor which is pre-filled with the template content.
     */
    @CommonSchemaAttributes(defaultValue = "edit")
    private CreateResultType createResult;

    public BlueprintModuleBean() {
        initialise();
    }

    public BlueprintModuleBean(BlueprintModuleBeanBuilder builder)
    {
        super(builder);
        initialise();
    }

    public static BlueprintModuleBeanBuilder newBlueprintModuleBean()
    {
        return new BlueprintModuleBeanBuilder();
    }

    public BlueprintTemplateBean getBlueprintTemplate()
    {
        return template;
    }

    public CreateResultType getCreateResult()
    {
        return createResult;
    }

    public IconBean getIcon()
    {
        return icon;
    }

    private void initialise() {
        if (null == createResult)
        {
            createResult = CreateResultType.EDIT;
        }
    }
}
