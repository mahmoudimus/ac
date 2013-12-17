package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.MacroEditorBeanBuilder;
import com.google.common.base.Strings;

/**
 * Macro Parameters go a long way when it comes to macro configuration, but there are cases
 * when a macro add-on needs more control over the UI.
 *
 * Defining a Macro Editor allows you to implement a custom UI for the macro, by specifying a
 * URL to a page in your add-on which will be shown in the dialog iframe.
 *
 * JSON Example:
 * @exampleJson {@see ConnectJsonExamples#MACRO_EDITOR_EXAMPLE}
 * @schemaTitle Macro Editor
 * @since 1.0
 */
public class MacroEditorBean extends BaseModuleBean
{
    /**
     * The URL to the macro configuration page in the add-on.
     */
    @Required
    @StringSchemaAttributes(format = "uri")
    private String url;

    /**
     * An optional title that will be shown in the edit dialog header for an existing macro.
     */
    private I18nProperty editTitle;

    /**
     * An optional title that will be shown in the edit dialog header for a new macro.
     */
    private I18nProperty insertTitle;

    /**
     * The preferred width of the edit dialog.
     */
    private String width;

    /**
     * The preferred height of the edit dialog.
     */
    private String height;

    public MacroEditorBean()
    {
        init();
    }

    public MacroEditorBean(MacroEditorBeanBuilder builder)
    {
        super(builder);
        init();
    }

    private void init()
    {
        if (null == url)
        {
            url = "";
        }
        if (null == editTitle)
        {
            this.editTitle = I18nProperty.empty();
        }
        if (null == insertTitle)
        {
            this.insertTitle = I18nProperty.empty();
        }
        if (null == width)
        {
            width = "";
        }
        if (null == height)
        {
            height = "";
        }
    }

    public String getUrl()
    {
        return url;
    }

    public I18nProperty getEditTitle()
    {
        return editTitle;
    }

    public boolean hasEditTitle()
    {
        return !Strings.isNullOrEmpty(editTitle.getValue());
    }

    public I18nProperty getInsertTitle()
    {
        return insertTitle;
    }

    public boolean hasInsertTitle()
    {
        return !Strings.isNullOrEmpty(insertTitle.getValue());
    }

    public String getWidth()
    {
        return width;
    }

    public String getHeight()
    {
        return height;
    }

    public static MacroEditorBeanBuilder newMacroEditorBean()
    {
        return new MacroEditorBeanBuilder();
    }

    public static MacroEditorBeanBuilder newMacroEditorBean(MacroEditorBean defaultBean)
    {
        return new MacroEditorBeanBuilder(defaultBean);
    }
}
