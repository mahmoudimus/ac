/**
 * In order for a Confluence macro to have a custom property panel, the override
 * behaviour needs to be registered with the Macro Browser's JavaScript model at runtime. This file is a template that
 * gets injected into the dynamically-generated add-on plugin for every Remote Macro module. The specifics of
 * each
 * individual macro are injected into the variable names via a Web Resource Transformer module
 * (see MacroVariableInjectorTransformer).
 */
AJS.bind("init.rte", function () {
    // These parameters are injected contextually by the MacroVariableInjectorTransformer
    var macroName = "%%MACRONAME%%";
    var macroUrl = AJS.params.contextPath + "%%URL%%";

    require(["ac/confluence/macro/property-panel-controls", "ac/confluence/macro/property-panel-iframe", "confluence/macro-js-overrides"], function(getControls, propertyPanelIframeInjector, macroOverrides) {
        macroOverrides.assignFunction(macroName, propertyPanelIframeInjector(macroUrl));
        macroOverrides.assignFunction(macroName, getControls(macroUrl));
    });
});
