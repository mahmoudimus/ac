package com.atlassian.plugin.connect.plugin.capabilities.beans.builder;

import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BaseContentMacroModuleBeanBuilder<T extends BaseContentMacroModuleBeanBuilder, B extends BaseContentMacroModuleBean> extends NameToKeyBeanBuilder<T, B>
{
    private String url;
    private I18nProperty description;
    private IconBean icon;
    private LinkBean documentation;
    private Set<String> categories;
    private MacroOutputType outputType;
    private MacroBodyType bodyType;
    private Set<String> aliases;
    private Boolean featured;
    private Integer width;
    private Integer height;
    private List<MacroParameterBean> parameters;

    public BaseContentMacroModuleBeanBuilder()
    {
    }

    public BaseContentMacroModuleBeanBuilder(B defaultBean)
    {
        this.url = defaultBean.getUrl();
        this.description = defaultBean.getDescription();
        this.icon = defaultBean.getIcon();
        this.documentation = defaultBean.getDocumentation();
        this.categories = defaultBean.getCategories();
        this.outputType = defaultBean.getOutputType();
        this.bodyType = defaultBean.getBodyType();
        this.aliases = defaultBean.getAliases();
        this.featured = defaultBean.isFeatured();
        this.width = defaultBean.getWidth();
        this.height = defaultBean.getHeight();
        this.parameters = defaultBean.getParameters();
    }

    public T withUrl(String url)
    {
        this.url = url;
        return (T) this;
    }

    public T withDescription(I18nProperty description)
    {
        this.description = description;
        return (T) this;
    }

    public T withIcon(IconBean icon)
    {
        this.icon = icon;
        return (T) this;
    }

    public T withDocumentation(LinkBean documentation)
    {
        this.documentation = documentation;
        return (T) this;
    }

    public T withCategories(String... categories)
    {
        this.categories = ImmutableSet.copyOf(categories);
        return (T) this;
    }

    public T withOutputType(MacroOutputType outputType)
    {
        this.outputType = outputType;
        return (T) this;
    }

    public T withBodyType(MacroBodyType bodyType)
    {
        this.bodyType = bodyType;
        return (T) this;
    }

    public T withAliases(String... aliases)
    {
        this.aliases = ImmutableSet.copyOf(aliases);
        return (T) this;
    }

    public T withFeatured(Boolean featured)
    {
        this.featured = featured;
        return (T) this;
    }

    public T withWidth(Integer width)
    {
        this.width = width;
        return (T) this;
    }

    public T withHeight(Integer height)
    {
        this.height = height;
        return (T) this;
    }

    public T withParameters(MacroParameterBean... parameters)
    {
        this.parameters = ImmutableList.copyOf(parameters);
        return (T) this;
    }

    public T withParameters(Collection<? extends MacroParameterBean> parameters)
    {
        this.parameters = ImmutableList.copyOf(parameters);
        return (T) this;
    }
}
