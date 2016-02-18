package com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.IconsBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;

public class UISupportBeanBuilder
        extends BaseModuleBeanBuilder<UISupportBeanBuilder, UISupportBean>
{
    private String viewComponent;
    private I18nProperty typeName;
    private IconsBean icons;

    public UISupportBeanBuilder()
    {
    }

    public UISupportBeanBuilder withViewComponent(String viewComponent)
    {
        this.viewComponent = viewComponent;
        return this;
    }

    public UISupportBeanBuilder withTypeName(I18nProperty typeName)
    {
        this.typeName = typeName;
        return this;
    }

    public UISupportBeanBuilder withIcons(String createDialog, String singleItem, String collectionItem)
    {
        this.icons = new IconsBean(createDialog, singleItem, collectionItem);
        return this;
    }

    public UISupportBean build()
    {
        return new UISupportBean(this);
    }
}
