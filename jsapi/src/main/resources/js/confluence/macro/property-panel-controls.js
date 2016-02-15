(function($, define){

    define("ac/confluence/macro/property-panel-controls", [], function() {
        return function(addonName, macroName) {
            var controls;
            return {
                getControls: function(callback) {
                    controls = controls || WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-macro.property-panel-controls");
                    callback(controls);
                }
            };
        };
    });
})(AJS.$, define);
