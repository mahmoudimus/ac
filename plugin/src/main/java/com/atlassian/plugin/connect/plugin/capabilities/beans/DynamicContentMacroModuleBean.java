package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.DynamicContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroCategory;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DynamicContentMacroModuleBean extends NameToKeyBean
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

    public DynamicContentMacroModuleBean()
    {
    }

    public DynamicContentMacroModuleBean(DynamicContentMacroModuleBeanBuilder builder)
    {
        super(builder);
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
        if (null == category)
        {
            category = MacroCategory.HIDDEN;
        }
        if (null == outputType)
        {
            outputType = MacroOutputType.INLINE;
        }
        if (null == bodyType)
        {
            bodyType = MacroBodyType.NONE;
        }
        if (null == alias)
        {
            alias = "";
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

    public MacroCategory getCategory()
    {
        return category;
    }

    public MacroOutputType getOutputType()
    {
        return outputType;
    }

    public MacroBodyType getBodyType()
    {
        return bodyType;
    }

    public String getAlias()
    {
        return alias;
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
