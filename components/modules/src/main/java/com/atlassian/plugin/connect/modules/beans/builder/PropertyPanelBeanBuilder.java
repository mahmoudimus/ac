package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.PropertyPanelBean;

public class PropertyPanelBeanBuilder extends BaseModuleBeanBuilder<PropertyPanelBeanBuilder, PropertyPanelBean>
{
    private String url;
    private I18nProperty editTitle;
    private I18nProperty insertTitle;
    private String width;
    private String height;

    public PropertyPanelBeanBuilder()
    {
    }

    public PropertyPanelBeanBuilder(PropertyPanelBean defaultBean)
    {
        this.url = defaultBean.getUrl();
        this.editTitle = defaultBean.getEditTitle();
        this.insertTitle = defaultBean.getInsertTitle();
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
    }

    public PropertyPanelBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public PropertyPanelBeanBuilder withEditTitle(I18nProperty editTitle)
    {
        this.editTitle = editTitle;
        return this;
    }

    public PropertyPanelBeanBuilder withInsertTitle(I18nProperty insertTitle)
    {
        this.insertTitle = insertTitle;
        return this;
    }

    public PropertyPanelBeanBuilder withWidth(String width)
    {
        this.width = width;
        return this;
    }

    public PropertyPanelBeanBuilder withHeight(String height)
    {
        this.height = height;
        return this;
    }

    @Override
    public PropertyPanelBean build()
    {
        return new PropertyPanelBean(this);
    }
}
