package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.*;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public abstract class BaseContentMacroModuleBean extends RequiredKeyBean
{
    /**
     * The link to the add-on resource that provides the macro content.
     * This URL has to be relative to the add-on base URL.
     *
     * Additional context parameters can be passed as variables in the URL:
     *
     *    {
     *        "url": "/macro-renderer?body={macro.body}&space_id={space.id}&page_id={page.id}"
     *    }
     *
     * Since macro bodies can be of arbitrary size and may contain sensitive data, care must be taken
     * as to how its passed to your connect addon.  You have three options to gain access to the body:
     *
     * - If you can predict the size of your body and it is consistently less than 128 characters, you
     * can include it in the GET request using the `{macro.body}` parameter.
     * - If you know your macro contains a body that will often exceed the 128 character threshold
     * (or is known to contain sensitive data), then you can include the `{macro.hash}` parameter and
     * use the Confluence REST api to call back to collect the body.
     * - If you want, you can include, `{macro.body}`, `{macro.hash}`, and `{macro.truncated}`. This way
     * your plugin can call back to confluence only if `{macro.truncated}` is '`true`'.  This will allow
     * you to skip the callback if it's not needed.  This would be useful for macros that don't
     * contain sensitive data of an unpredictable size.
     *
     *
     *__Note:__ If you include the `{macro.body}` in your URL you are potentially leaking sensitive data
     * to any intermediate host on the internet.  This may result in the body being cached or indexed
     * by a third party.  If you are concerned about the security of your macro, you should always use
     * the `{macro.hash}` and use the Confluence REST API to collect the body.
     *
     * Here's an example:
     *
     * Declare the variables that are later required to fetch the macro content in the URL:
     *
     *    {
     *        "url": "/render-macro?pageId={page.id}&pageVersion={page.version}&macroHash={macro.hash}"
     *    }
     *
     * Then use the Confluence REST API to collect the body, for example directly from the iframe:
     *
     *    AP.require("request", function(request) {
     *        var pageId = getUrlParameter("pageId");
     *        var pageVersion = getUrlParameter("pageVersion");
     *        var macroHash = getUrlParameter("macroHash");
     *        request({
     *            url: "/rest/api/content/" + pageId +
     *                 "/history/" + pageVersion +
     *                 "/macro/hash/" + macroHash,
     *            success: function(response) {
     *                var macro = JSON.parse(response);
     *                process(macro.body);
     *            }
     *        });
     *    });
     *
     * __Preview Mode:__ If you use the `{macro.hash}` in your URL, the REST api will not return the macro body during
     * a preview request, because the page has not been saved yet. You can use the `{output.type}` parameter to detect
     * whether the macro is rendered in preview mode and adapt the response accordingly.
     *
     * Currently supported variables for macros are:
     *
     * - `macro.hash`: The hash of the macro body
     * - `macro.body`: The macro body, truncated to 128 characters
     * - `macro.truncated`: True if the macro body was truncated, false of not
     * - `page.id`: The page ID, e.g. `1376295`
     * - `page.title`: The page title, e.g. `My Page`
     * - `page.type`: The page type, e.g. `page`
     * - `page.version`: The page version, e.g. `6`
     * - `space.id`: The space ID, e.g. `65537`
     * - `space.key`: The space key, e.g. `AC`
     * - `user.id`: The user ID, e.g. `admin`
     * - `user.key`: The user key, e.g. `ff80808143087d180143087d3a910004`
     * - `output.type`: The output type, e.g. `display` or `preview`
     *
     *Context parameters for macros are also required in the URL. Please see the
     * [Macro Input Parameter](../fragment/macro-input-parameter.html) documentation for details.
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
     * Whether the macro should be "featured", meaning having an additional link in the "Insert More Content" menu in the editor toolbar.
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean featured;

    /**
     * If set to true, the macro will not appear in the macro browser.
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean hidden;

    /**
     * The list of parameter input fields that will be displayed.
     */
    private List<MacroParameterBean> parameters;

    /**
     *  URL patterns associated with this macro. If a URL matching a defined pattern is pasted into the editor, this macro
     *  will be created and will replace the URL string.
     */
    private AutoconvertBean autoconvert;

    /**
     * The configuration of a custom macro editor. This is useful if the parameter input field types are
     * not sufficient to configure the macro.
     */

    private MacroEditorBean editor;

    /**
     * The image rendered in the editor as the macro placeholder. It can only be used with bodyless macros and will behave
     * just like a regular macro placeholder. Any parameter changes in the macro browser will cause the image
     * to be reloaded - so that changes can be seen.
     */
    private ImagePlaceholderBean imagePlaceholder;

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
        if (null == parameters)
        {
            parameters = newArrayList();
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

    public AutoconvertBean getAutoconvert()
    {
        return autoconvert;
    }

    public Boolean isFeatured()
    {
        return (null != featured) && featured;
    }

    public Boolean isHidden()
    {
        return (null != hidden) && hidden;
    }

    public List<MacroParameterBean> getParameters()
    {
        return parameters;
    }

    public MacroEditorBean getEditor()
    {
        return editor;
    }

    public ImagePlaceholderBean getImagePlaceholder()
    {
        return imagePlaceholder;
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

    public boolean hasImagePlaceholder()
    {
        return imagePlaceholder != null;
    }

    public boolean hasAutoConvert()
    {
        return (null != autoconvert);
    }
}
