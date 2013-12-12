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

/**
 * A Confluence macro that loads the remote content as an IFrame.
 * Dynamic Content Macros render content on every
 * page request and are suitable for Add-Ons that need to display content
 * that changes over time, or content is specific to the authenticated user.
 *
 * Json Example:
 * @exampleJson {@see ConnectJsonExamples#DYNAMIC_MACRO_EXAMPLE}
 * @schemaTitle Dynamic Content Macro
 * @since 1.0
 */
public class DynamicContentMacroModuleBean extends NameToKeyBean
{
    /**
     * The link to the Add-On resource that provides the content for the iFrame.
     * This URL has to be relative to the Add-On base URL.
     */
    private String url;

    /**
     * A description of the macro's functionality.
     */
    private I18nProperty description;

    /**
     * A link to the icon resource (80x80 pixels)that will be shown in the macro selection dialog.
     * The URL can be absolute to relative to the Add-On base URL.
     */
    private IconBean icon;

    /**
     *  A link to the documentation for the macro. The URL can be absolute to relative to the Add-On base URL.
     */
    private String documentationUrl;

    /**
     * The categories the macro should appear in. A macro with no category will show up in the default 'All' category.
     */
    private Set<MacroCategory> categories;

    /**
     * How this macro should be placed along side other page content.
     */
    private MacroOutputType outputType;

    /**
     * The type of body content, if any, for this macro.
     */
    private MacroBodyType bodyType;

    /**
     * Define aliases for the macro. The macro browser will open for the defined aliases as if it were this macro.
     */
    private Set<String> aliases;

    /**
     * Whether the macro should be "featured", meaning having an additional link in the "Insert More Content" menu in the editor toolbar
     */
    private Boolean featured;

    /**
     * The preferred width of the macro content.
     */
    private Integer width;

    /**
     * The preferred height of the macro content.
     */
    private Integer height;

    /**
     * The list of parameter input fields that will be displayed.
     */
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
