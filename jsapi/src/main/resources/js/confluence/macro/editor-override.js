/**
 * In order for a Confluence macro to have a custom editing experience (ie. overriding the macro browser), the override
 * behaviour needs to be registered with the Macro Browser's JavaScript model at runtime. This file is a template that
 * gets injected into the dynamically-generated Remotable Plugin plugin for every Remote Macro module. The specifics of each
 * individual macro are injected into the variable names via a Web Resource Transformer module
 * (see MacroVariableInjectorTransformer).
 */
AJS.bind("init.rte", function () {
    // These parameters are injected contextually by the MacroVariableInjectorTransformer
    var macroName = "%%MACRONAME%%";
    var editorOpts = {
      width: "%%WIDTH%%",
      height: "%%HEIGHT%%",
      editTitle: "%%EDIT_TITLE%%",
      insertTitle: "%%INSERT_TITLE%%",
      url: AJS.params.contextPath + "%%URL%%"
    };
    require(["ac/confluence/macro/editor"], function(macroEditor) {
        //TODO: Replace this with confluence/macro-js-overrides, once the version of Confluence that supports
        // it is released to cloud. CRA-1219
        var existingOverride = AJS.MacroBrowser.getMacroJsOverride(macroName);
        if (existingOverride == null) {
            existingOverride = {};
        }
        $.extend(existingOverride, {
            opener: function(macroData) {
                macroData = $.extend({name: macroName}, macroData);
                macroEditor.openCustomEditor(macroData, editorOpts);
            }
        });
        AJS.MacroBrowser.setMacroJsOverride(macroName, existingOverride);
    });

});
