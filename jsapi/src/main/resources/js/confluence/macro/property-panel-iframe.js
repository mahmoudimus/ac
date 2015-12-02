(function($, define){

    define("ac/confluence/macro/property-panel-iframe", ["connect-host"], function(_AP) {
        return function(macroUrl) {
            return {
                propertyPanelIFrameInjector: function(currentPropertyPanel) {
                    var data = {
                        "ui-params": _AP.uiParams.encode({dlg: 1}),
                        "classifier": "property-panel"
                    };

                    $.ajax(macroUrl, {
                        data: data
                    }).done(
                        function(data){
                            var panelHtml = $(data);
                            panelHtml.css("display", "none");
                            currentPropertyPanel.panel.append(panelHtml);
                        }
                    );
                }
            };
        };
    });
})(AJS.$, define);
