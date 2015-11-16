package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MacroEditorBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.MacroPropertyPanelBeanBuilder;

/**
 *
 * Defining a Macro Property panel allows you to add controls other than 'edit', and 'remove' to your macro's
 * property panel. You can then specify what effect those buttons have in javascript defined in your property panel
 * iframe.
 *
 * In order to persist custom data using your property panel, use the Javascript
 * [Confluence API](../../javascript/module-confluence.html) and the [Dialog API](../../javascript/module-Dialog.html).
 * For example:
 *
 *    AP.require(["confluence", "dialog"], function (confluence, dialog) {
 *        function onSubmit() {
 *            var macroParams = {
 *                myParameter: value
 *            };
 *            confluence.saveMacro(macroParams);
 *            confluence.closePropertyPanel();
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
 * TODO: Replace this example JSON
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#MACRO_EDITOR_EXAMPLE}
 * @schemaTitle Macro Property Panel
 * @since 1.0
 */
public class MacroPropertyPanelBean extends BaseModuleBean
{
    /**
     * The URL to the event handling page in the add-on.
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    public MacroPropertyPanelBean()
    {
        init();
    }

    public MacroPropertyPanelBean(MacroPropertyPanelBeanBuilder builder)
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
    }

    public String getUrl()
    {
        return url;
    }

    public static MacroPropertyPanelBeanBuilder newMacroPropertyPanelBean()
    {
        return new MacroPropertyPanelBeanBuilder();
    }

    public static MacroPropertyPanelBeanBuilder newMacroPropertyPanelBean(MacroPropertyPanelBean defaultBean)
    {
        return new MacroPropertyPanelBeanBuilder(defaultBean);
    }
}
