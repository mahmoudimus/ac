package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MacroEditorBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.MacroPropertyPanelBeanBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 *
 * Defining a Macro Property panel allows you to add a hidden iframe to your macro's
 * property panel. The iframe is loaded as soon as the property panel is opened.
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
 * Dialogs may also be created. Use `dialog.create` (see
 * [Dialog API](../../javascript/module-Dialog.html)):
 *
 *    AP.require('dialog', function(dialog) {
 *        dialog.create({
 *            key: 'my-module-key',
 *            width: '500px',
 *            height: '200px',
 *            chrome: true
 *        }).on("close", callbackFunc);
 *    });
 *
 * @schemaTitle Macro Property Panel
 */
public class MacroPropertyPanelBean extends BaseModuleBean
{
    /**
     * The URL to the event handling page in the add-on.
     */
    @Required
    @StringSchemaAttributes(format = "uri-template")
    private String url;

    /**
     * List of controls which will be added to the macro property panel
     */
    private List<ControlBean> controls;

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

        if (null == controls)
        {
            controls = ImmutableList.of();
        }
    }

    public String getUrl()
    {
        return url;
    }

    public List<ControlBean> getControls()
    {
        return controls;
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
