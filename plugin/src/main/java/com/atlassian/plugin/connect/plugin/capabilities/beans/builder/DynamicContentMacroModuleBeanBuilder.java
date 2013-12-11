package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroCategory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

public class DynamicContentMacroModuleBeanBuilder extends NameToKeyBeanBuilder<DynamicContentMacroModuleBeanBuilder, DynamicContentMacroModuleBean>
{
    private String url;
    private I18nProperty description;
    private IconBean icon;
    private String documentationUrl;
    private MacroCategory category;
    private MacroOutputType outputType;
    private MacroBodyType bodyType;
    private String alias;
    private Boolean featured;
    private Integer width;
    private Integer height;
    private List<MacroParameterBean> parameters;

    public DynamicContentMacroModuleBeanBuilder()
    {
    }

    public DynamicContentMacroModuleBeanBuilder(DynamicContentMacroModuleBean defaultBean)
    {
        this.url = defaultBean.getUrl();
        this.description = defaultBean.getDescription();
        this.icon = defaultBean.getIcon();
        this.documentationUrl = defaultBean.getDocumentationUrl();
        this.category = defaultBean.getCategory();
        this.outputType = defaultBean.getOutputType();
        this.bodyType = defaultBean.getBodyType();
        this.alias = defaultBean.getAlias();
        this.featured = defaultBean.getFeatured();
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
        this.parameters = defaultBean.getParameters();
    }

    public DynamicContentMacroModuleBeanBuilder withUrl(String url)
    {
        this.url = url;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withDescription(I18nProperty description)
    {
        this.description = description;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withIcon(IconBean icon)
    {
        this.icon = icon;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withDocumentationUrl(String documentationUrl)
    {
        this.documentationUrl = documentationUrl;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withCategory(MacroCategory category)
    {
        this.category = category;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withOutputType(MacroOutputType outputType)
    {
        this.outputType = outputType;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withBodyType(MacroBodyType bodyType)
    {
        this.bodyType = bodyType;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withAlias(String alias)
    {
        this.alias = alias;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withFeatured(Boolean featured)
    {
        this.featured = featured;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withWidth(Integer width)
    {
        this.width = width;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withHeight(Integer height)
    {
        this.height = height;
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withParameters(MacroParameterBean... parameters)
    {
        this.parameters = Lists.newArrayList(parameters);
        return this;
    }

    public DynamicContentMacroModuleBeanBuilder withParameters(Collection<? extends MacroParameterBean> parameters)
    {
        this.parameters = Lists.newArrayList(parameters);
        return this;
    }

    @Override
    public DynamicContentMacroModuleBean build()
    {
        return new DynamicContentMacroModuleBean(this);
    }
}
