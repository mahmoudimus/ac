package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class BaseContentMacroModuleBeanBuilder<T extends BaseContentMacroModuleBeanBuilder, B extends BaseContentMacroModuleBean> extends RequiredKeyBeanBuilder<T, B>
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
    private Boolean hidden;
    private List<MacroParameterBean> parameters;
    private MacroEditorBean editor;
    private ImagePlaceholderBean imagePlaceholder;

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
        this.hidden = defaultBean.isHidden();
        this.parameters = defaultBean.getParameters();
        this.editor = defaultBean.getEditor();
        this.imagePlaceholder = defaultBean.getImagePlaceholder();
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

    public T withHidden(Boolean hidden)
    {
        this.hidden = hidden;
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

    public T withEditor(MacroEditorBean editor)
    {
        this.editor = editor;
        return (T) this;
    }

    public T withImagePlaceholder(ImagePlaceholderBean imagePlaceholder)
    {
        this.imagePlaceholder = imagePlaceholder;
        return (T) this;
    }
}
