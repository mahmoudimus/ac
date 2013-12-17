package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.MacroEditorBean.newMacroEditorBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty.empty;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean.newLinkBean;

public abstract class BaseContentMacroModuleBean extends NameToKeyBean
{
    /**
     * The link to the add-on resource that provides the content for the iFrame.
     * This URL has to be relative to the add-on base URL.
     */
    @Required
    private String url;

    /**
     * A description of the macro's functionality.
     */
    private I18nProperty description;

    /**
     * A link to the icon resource (80x80 pixels) that will be shown in the macro selection dialog.
     * The URL can be absolute or relative to the add-on base URL.
     */
    private IconBean icon;

    /**
     * A link to the documentation for the macro.
     */
    private LinkBean documentation;

    /**
     * The categories the macro should appear in. A macro with no category will show up in the default 'All' category.
     *
     * Currently, the following categories are supported by Confluence:
     *
     * * __admin__: Administration
     * * __communication__: Communication
     * * __confluence-content__: Confluence Content
     * * __development__: Development
     * * __external-content__: External Content
     * * __formatting__: Formatting
     * * __hidden-macros__: Hidden
     * * __media__: Media
     * * __navigation__: Navigation
     * * __reporting__: Reporting
     * * __visuals__: Visuals & Images
     */
    private Set<String> categories;

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
    private String width;

    /**
     * The preferred height of the macro content.
     */
    private String height;

    /**
     * The list of parameter input fields that will be displayed.
     */
    private List<MacroParameterBean> parameters;

    /**
     * The configuration of a custom macro editor. This is useful if the parameter input field types are
     * not sufficient to configure the macro.
     */
    private MacroEditorBean editor;


    public BaseContentMacroModuleBean()
    {
        initialize();
    }

    public BaseContentMacroModuleBean(BaseContentMacroModuleBeanBuilder builder)
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
            icon = newIconBean().build();
        }
        if (null == description)
        {
            description = empty();
        }
        if (null == documentation)
        {
            documentation = newLinkBean().build();
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
            width = "";
        }
        if (null == height)
        {
            height = "";
        }
        if (null == parameters)
        {
            parameters = Lists.newArrayList();
        }
        if (null == editor)
        {
            editor = newMacroEditorBean().build();
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

    public LinkBean getDocumentation()
    {
        return documentation;
    }

    public Set<String> getCategories()
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

    public Boolean isFeatured()
    {
        return featured;
    }

    public String getWidth()
    {
        return width;
    }

    public String getHeight()
    {
        return height;
    }

    public List<MacroParameterBean> getParameters()
    {
        return parameters;
    }

    public MacroEditorBean getEditor()
    {
        return editor;
    }

    public boolean hasEditor()
    {
        return editor.hasUrl();
    }

    public boolean hasIcon()
    {
        return icon.hasUrl();
    }

    public boolean hasDocumentation()
    {
        return documentation.hasUrl();
    }
}
