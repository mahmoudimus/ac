package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

/**
 * Blueprints allow your connect add on to provide content creation templates.
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#BLUEPRINT_EXAMPLE}
 * @schemaTitle Remote Blueprints
 * @since 1.1.5
 */
public class BlueprintModuleBean extends RequiredKeyBean {


    private IconBean icon;

    @Required
    private BlueprintTemplateBean template;

    public BlueprintModuleBean() {
    }

    public BlueprintModuleBean(BlueprintModuleBeanBuilder builder) {
        super(builder);
    }

    public BlueprintTemplateBean getBlueprintTemplate() {
        return template;
    }

    public static BlueprintModuleBeanBuilder newBlueprintModuleBean() {
        return new BlueprintModuleBeanBuilder();
    }

    public IconBean getIcon() {
        return icon;
    }
}
