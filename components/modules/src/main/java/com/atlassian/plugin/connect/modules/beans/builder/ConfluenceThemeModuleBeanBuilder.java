package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.ConfluenceThemeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.IconBean;
import com.atlassian.plugin.connect.modules.beans.nested.UiOverrideBean;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ConfluenceThemeModuleBeanBuilder extends RequiredKeyBeanBuilder<ConfluenceThemeModuleBeanBuilder, ConfluenceThemeModuleBean>
{

//    private I18nProperty description;

    private List<UiOverrideBean> overrides;

    private IconBean icon;


    public ConfluenceThemeModuleBeanBuilder withOverrides(List<UiOverrideBean> overrides)
    {
        this.overrides = new ArrayList<>(overrides);
        return this;
    }

    public ConfluenceThemeModuleBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
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
