package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.plugin.connect.modules.beans.builder.BlueprintModuleBeanBuilder;

/**
 * Blueprints allow your connect add on to provide content creation templates.
 */
public class BlueprintModuleBean extends BeanWithKeyAndParams {

    public static BlueprintModuleBeanBuilder newWebItemBean()
    {
        return new BlueprintModuleBeanBuilder();
    }

    public static BlueprintModuleBeanBuilder newWebItemBean(BlueprintModuleBean defaultBean)
    {
        return new BlueprintModuleBeanBuilder(defaultBean);
    }
}
