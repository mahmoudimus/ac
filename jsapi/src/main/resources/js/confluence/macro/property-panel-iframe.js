(function($, define){

    define("ac/confluence/macro/property-panel-iframe", ["connect-host", "ac/confluence/macro", "ajs"], function(_AP, saveMacro, AJS) {
        return function(macroUrl) {
            return {
                propertyPanelIFrameInjector: function(propertyPanel) {
                    var data = {
                        "ui-params": _AP.uiParams.encode({dlg: 1}),
                        "classifier": "property-panel"
                    };

                    var editorSelection = AJS.Rte.getEditor().selection;
                    saveMacro.setLastSelectedConnectMacroNode(editorSelection.getNode());

                    $.ajax(macroUrl, {
                        data: data
                    }).done(
                        function(data){
                            var panelHtml = $(data);
                            panelHtml.css("display", "none");
                            propertyPanel.panel.append(panelHtml);
                        }
                    );
                }
            };
        };
    });
})(AJS.$, define);
