package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;

/**
 * Blueprints allow your connect add on to provide content creation templates.
 */
public class BlueprintModuleBean extends RequiredKeyBean {

    public BlueprintModuleBean() {
    }

    public BlueprintModuleBean(BlueprintModuleBeanBuilder builder) {
        super(builder);
    }

    public static BlueprintModuleBeanBuilder newBlueprintModuleBean()
    {
        return new BlueprintModuleBeanBuilder();
    }

    public static BlueprintModuleBeanBuilder newBlueprintModuleBean(BlueprintModuleBean defaultBean)
    {
        return new BlueprintModuleBeanBuilder(defaultBean);
    }
}
