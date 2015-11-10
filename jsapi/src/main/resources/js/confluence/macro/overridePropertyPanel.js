/**
 * In order for a Confluence macro to have a custom editing experience (ie. overriding the macro browser), the override
 * behaviour needs to be registered with the Macro Browser's JavaScript model at runtime. This file is a template that
 * gets injected into the dynamically-generated Remotable Plugin plugin for every Remote Macro module. The specifics of each
 * individual macro are injected into the variable names via a Web Resource Transformer module
 * (see MacroVariableInjectorTransformer).
 */
AJS.bind("init.rte", function () {
    console.log("binding macro editor: " + AJS.params.contextPath + "%%URL%%");
    // These parameters are injected contextually by the MacroVariableInjectorTransformer
    var macroName = "%%MACRONAME%%";
    var editorOpts = {
        width: "%%WIDTH%%",
        height: "%%HEIGHT%%",
        editTitle: "%%EDIT_TITLE%%",
        insertTitle: "%%INSERT_TITLE%%",
        url: AJS.params.contextPath + "%%URL%%"
    };
    require(["ac/confluence/macro/property-panel"], function(propertyPanel) {
        var existing = AJS.MacroBrowser.getMacroJsOverride(macroName);
        if (existing == null) {
            existing = {};
        }
        $.extend(existing, {
            propertyPanelOpener: function(macroData) {
                macroData = $.extend({name: macroName}, macroData);
                propertyPanel.openCustomPropertyPanel(macroData, editorOpts);
            }
        });
        AJS.MacroBrowser.setMacroJsOverride(macroName, existing);
    });
});
