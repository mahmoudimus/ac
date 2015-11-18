/**
 * In order for a Confluence macro to have a custom property panel, the override
 * behaviour needs to be registered with the Macro Browser's JavaScript model at runtime. This file is a template that
 * gets injected into the dynamically-generated Remotable Plugin plugin for every Remote Macro module. The specifics of each
 * individual macro are injected into the variable names via a Web Resource Transformer module
 * (see MacroVariableInjectorTransformer).
 */
AJS.bind("init.rte", function () {
    console.log("binding property panel iframe: " + AJS.params.contextPath + "%%URL%%");
    // These parameters are injected contextually by the MacroVariableInjectorTransformer
    var macroName = "%%MACRONAME%%";
    var macroUrl = AJS.params.contextPath + "%%URL%%";

    require(["connect-host"], function(_AP) {
        var existingOverride = AJS.MacroBrowser.getMacroJsOverride(macroName);
        if (existingOverride == null) {
            existingOverride = {};
        }
        $.extend(existingOverride, {
            propertyPanelIFrameInjector: function(currentPropertyPanel) {
                function getIframeHtmlForMacro(url) {
                    var data = {
                        "ui-params": _AP.uiParams.encode({dlg: 1}),
                        "classifier": "property-panel"
                    };
                    return $.ajax(url, {
                        data: data
                    });
                }

                getIframeHtmlForMacro(macroUrl).done(function(data){
                    var panelHtml = $(data);
                    panelHtml.css("display", "none");
                    currentPropertyPanel.panel.append(panelHtml);
                });
            }
        });
        AJS.MacroBrowser.setMacroJsOverride(macroName, existingOverride);
    });

});
