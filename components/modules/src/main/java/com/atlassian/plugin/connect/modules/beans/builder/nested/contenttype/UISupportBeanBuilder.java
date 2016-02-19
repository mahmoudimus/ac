package com.atlassian.plugin.connect.modules.beans.builder.nested.contenttype;

import com.atlassian.plugin.connect.modules.beans.builder.BaseModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.IconsBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;

public class UISupportBeanBuilder
        extends BaseModuleBeanBuilder<UISupportBeanBuilder, UISupportBean>
{
    private String contentViewComponent;

    private String editViewComponent;

    private String containerViewComponent;

    private String titleDisplay;

    private String titleSortValue;

    private IconsBean icons;

    public UISupportBeanBuilder()
    {
    }

    public UISupportBeanBuilder withViewComponent(String viewComponent)
    {
        this.contentViewComponent = viewComponent;
        return this;
    }

    public UISupportBeanBuilder withEditViewComponent(String editViewComponent)
    {
        this.editViewComponent = editViewComponent;
        return this;
    }

    public UISupportBeanBuilder withContainerViewComponent(String containerViewComponent)
    {
        this.containerViewComponent = containerViewComponent;
        return this;
    }

    public UISupportBeanBuilder withTitleDisplay(String titleDisplay)
    {
        this.titleDisplay = titleDisplay;
        return this;
    }

    public UISupportBeanBuilder withTitleSortValue(String titleSortValue)
    {
        this.titleSortValue = titleSortValue;
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
