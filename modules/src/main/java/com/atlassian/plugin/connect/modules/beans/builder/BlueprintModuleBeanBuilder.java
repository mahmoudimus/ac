package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;

public class BlueprintModuleBeanBuilder extends RequiredKeyBeanBuilder<BlueprintModuleBeanBuilder,BlueprintModuleBean>
{

    private IconBean icon;
    private BlueprintTemplateBean template;
    private String createResult;

    public BlueprintModuleBeanBuilder() { }

    public BlueprintModuleBeanBuilder withTemplate(BlueprintTemplateBean template)
    {
        this.template = template;
        return this;
    }

    public BlueprintModuleBeanBuilder withCreateResult(String createResult)
    {
        this.createResult = createResult;
        return this;
    }

    public BlueprintModuleBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    public BlueprintModuleBean build() {
        return new BlueprintModuleBean(this);
    }
}
