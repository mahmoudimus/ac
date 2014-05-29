package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

/**
 * Blueprints allow your connect add on to provide content creation templates.
 */
public class BlueprintModuleBean extends RequiredKeyBean {

    private IconBean icon;
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
