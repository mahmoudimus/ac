package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroCategory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public class DynamicContentMacroModuleBean extends NameToKeyBean
{
    private String url;
    private I18nProperty description;
    private IconBean icon;
    private String documentationUrl;
    private Set<MacroCategory> categories;
    private MacroOutputType outputType;
    private MacroBodyType bodyType;
    private Set<String> aliases;
    private Boolean featured;
    private Integer width;
    private Integer height;
    private List<MacroParameterBean> parameters;

    public DynamicContentMacroModuleBean()
    {
        initialize();
    }

    public DynamicContentMacroModuleBean(DynamicContentMacroModuleBeanBuilder builder)
    {
        super(builder);
        initialize();
    }

    private void initialize()
    {
        if (null == url)
        {
            url = "";
        }
        if (null == icon)
        {
            icon = IconBean.newIconBean().build();
        }
        if (null == description)
        {
            description = I18nProperty.empty();
        }
        if (null == documentationUrl)
        {
            documentationUrl = "";
        }
        if (null == categories)
        {
            categories = ImmutableSet.of();
        }
        if (null == outputType)
        {
            outputType = MacroOutputType.INLINE;
        }
        if (null == bodyType)
        {
            bodyType = MacroBodyType.NONE;
        }
        if (null == aliases)
        {
            aliases = ImmutableSet.of();
        }
        if (null == featured)
        {
            featured = false;
        }
        if (null == width)
        {
            width = 0;
        }
        if (null == height)
        {
            height = 0;
        }
        if (null == parameters)
        {
            parameters = Lists.newArrayList();
        }
    }

    public String getUrl()
    {
        return url;
    }

    public URI createUri() throws URISyntaxException
    {
        return null == url ? null : new URI(url);
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public IconBean getIcon()
    {
        return icon;
    }

    public String getDocumentationUrl()
    {
        return documentationUrl;
    }

    public Set<MacroCategory> getCategories()
    {
        return categories;
    }

    public MacroOutputType getOutputType()
    {
        return outputType;
    }

    public MacroBodyType getBodyType()
    {
        return bodyType;
    }

    public Set<String> getAliases()
    {
        return aliases;
    }

    public Boolean getFeatured()
    {
        return featured;
    }

    public Integer getWidth()
    {
        return width;
    }

    public Integer getHeight()
    {
        return height;
    }

    public List<MacroParameterBean> getParameters()
    {
        return parameters;
    }

    public static DynamicContentMacroModuleBeanBuilder newDynamicContentMacroModuleBean()
    {
        return new DynamicContentMacroModuleBeanBuilder();
    }

    public static DynamicContentMacroModuleBeanBuilder newDynamicContentMacroModuleBean(DynamicContentMacroModuleBean defaultBean)
    {
        return new DynamicContentMacroModuleBeanBuilder(defaultBean);
    }


}
