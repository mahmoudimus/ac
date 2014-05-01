package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MacroEditorBeanBuilder;

/**
 * Macro Parameters go a long way when it comes to macro configuration, but there are cases
 * when a macro add-on needs more control over the UI.
 *
 * Defining a Macro Editor allows you to implement a custom UI for the macro, by specifying a
 * URL to a page in your add-on which will be shown in the dialog iFrame.
 *
 * In order to persist custom data in your macro editor, use the Javascript
 * [Confluence API](../../javascript/module-confluence.html) and the [Dialog API](../../javascript/module-dialog.html).
 * For example:
 *
 *
 *    AP.require(["confluence", "dialog"], function (confluence, dialog) {
 *        function onSubmit() {
 *            var macroParams = {
 *                myParameter: value
 *            };
 *            confluence.saveMacro(macroParams);
 *            confluence.closeMacroEditor();
 *            return true;
 *        }
 *
 *        dialog.getButton("submit").bind(onSubmit);
 *    });
 *
 * In order to retrieve the custom data again when the editor is opened, use `confluence.getMacroData` (see
 * [Confluence API](../../javascript/module-confluence.html)):
 *
 *    AP.require("confluence", function (confluence) {
 *        var macroData = confluence.getMacroData(function(macroParams) {
 *            domeSomethingWith(macroParams.myParameter);
 *        });
 *    });
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#MACRO_EDITOR_EXAMPLE}
 * @schemaTitle Macro Editor
 * @since 1.0
 */
public class MacroEditorBean extends BaseModuleBean
{
    /**
     * The URL to the macro configuration page in the add-on.
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    /**
     * An optional title that will be shown in the edit dialog header for an existing macro.
     * Confluence renders this as "Edit '{editTitle}' Macro".
     */
    private I18nProperty editTitle;

    /**
     * An optional title that will be shown in the edit dialog header for a new macro.
     * Confluence renders this as "Insert '{insertTitle}' Macro".
     */
    private I18nProperty insertTitle;

    /**
     * The preferred width of the edit dialog, e.g. ``500px``.
     */
    private String width;

    /**
     * The preferred height of the edit dialog, e.g. ``300px``.
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
        return null != editTitle;
    }

    public I18nProperty getInsertTitle()
    {
        return insertTitle;
    }

    public boolean hasInsertTitle()
    {
        return null != editTitle;
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
