package com.atlassian.plugin.connect.modules.beans.builder;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;

/**
 *
 */
public class ConfluenceThemeModuleBeanBuilder extends RequiredKeyBeanBuilder<ConfluenceThemeModuleBeanBuilder, ConfluenceThemeModuleBean>
{

//    private I18nProperty description;

    private List<UiOverrideBean> overrides;

    public ConfluenceThemeModuleBeanBuilder withOverrides(List<UiOverrideBean> overrides)
    {
        this.overrides = new ArrayList<>(overrides);
        return this;
    }

//    public ConfluenceThemeModuleBeanBuilder withDescription(I18nProperty description) {
//        this.description = description;
//        return this;
//    }
    public ConfluenceThemeModuleBean build()
    {
        return new ConfluenceThemeModuleBean(this);
    }

}
