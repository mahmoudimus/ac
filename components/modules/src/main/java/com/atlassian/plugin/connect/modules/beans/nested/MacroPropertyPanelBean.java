package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MacroEditorBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.MacroPropertyPanelBeanBuilder;

/**
 *
 * Defining a Macro Property panel allows you to add a hidden iframe to your macro's
 * property panel.
 *
 * In order to persist custom data using your property panel, use the Javascript
 * [Confluence API](../../javascript/module-confluence.html).
 * For example:
 *
 *    AP.require(["confluence"], function (confluence) {
 *        var macroParams = {
 *            myParameter: value
 *        };
 *        confluence.saveMacro(macroParams);
 *    });
 *
 * In order to retrieve the custom data again when the property panel is opened, use `confluence.getMacroData` (see
 * [Confluence API](../../javascript/module-confluence.html)):
 *
 *    AP.require("confluence", function (confluence) {
 *        var macroData = confluence.getMacroData(function(macroParams) {
 *            doSomethingWith(macroParams.myParameter);
 *        });
 *    });
 *
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
