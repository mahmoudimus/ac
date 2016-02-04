(function($, define){

    define("ac/confluence/macro/property-panel-controls", [], function() {
        return function(macroUrl) {
            return {
                getControls: function(callback) {
                    callback([
                        {
                            className: "macro-placeholder-property-panel-hello-button",
                            text: "Hello"
                        }
                    ]);
                }
            };
        };
    });
})(AJS.$, define);
