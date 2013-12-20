package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.LinkBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroEditorBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroOutputType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.MacroParameterBean;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty.empty;

public abstract class BaseContentMacroModuleBean extends NameToKeyBean
{
    /**
     * The link to the add-on resource that provides the content for the iFrame.
     * This URL has to be relative to the add-on base URL.
     *
     * The macro body and additional context parameters can be passed as variables in the URL.
     *
     * ```
     * "url": "/macro-renderer?body={body}&space_id={space.id}&page_id={page.id}"
     * ```
     *
     * Currently supported variables for macros are:
     *
     * * `body`: The macro body
     * * `page.id`: The page ID, e.g. '1376295'
     * * `page.title`: The page title, e.g. 'My Page'
     * * `page.type`: The page type, e.g. 'page'
     * * `page.version.id`: The page version, e.g. '6'
     * * `space.id`: The space ID, e.g. 'ac'
     * * `space.key`: The space key, e.g. '65537'
     * * `user.id`: The user ID, e.g. 'admin'
     * * `user.key`: The user key, e.g. 'ff80808143087d180143087d3a910004'
     * * `output.type`: The output type, e.g. 'display'
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
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
     * * `admin`: Administration
     * * `communication`: Communication
     * * `confluence-content`: Confluence Content
     * * `development`: Development
     * * `external-content`: External Content
     * * `formatting`: Formatting
     * * `hidden-macros`: Hidden
     * * `media`: Media
     * * `navigation`: Navigation
     * * `reporting`: Reporting
     * * `visuals`: Visuals & Images
     */
    private Set<String> categories;

    /**
     * How this macro should be placed along side other page content.
     */
    @CommonSchemaAttributes(defaultValue = "block")
    private MacroOutputType outputType;

    /**
     * The type of body content, if any, for this macro.
     */
    @CommonSchemaAttributes(defaultValue = "none")
    private MacroBodyType bodyType;

    /**
     * Define aliases for the macro. The macro browser will open for the defined aliases as if it were this macro.
     */
    private Set<String> aliases;

    /**
     * Whether the macro should be "featured", meaning having an additional link in the "Insert More Content" menu in the editor toolbar
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean featured;

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
        if (null == description)
        {
            description = empty();
        }
        if (null == categories)
        {
            categories = ImmutableSet.of();
        }
        if (null == outputType)
        {
            outputType = MacroOutputType.BLOCK;
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
        if (null == parameters)
        {
            parameters = Lists.newArrayList();
        }
    }

    public String getUrl()
    {
        return url;
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
        return editor != null;
    }

    public boolean hasIcon()
    {
        return icon != null;
    }

    public boolean hasDocumentation()
    {
        return documentation != null;
    }
}
