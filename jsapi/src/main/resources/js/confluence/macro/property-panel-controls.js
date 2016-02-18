(function($, define){

    define("ac/confluence/macro/property-panel-controls", [
        'underscore'
    ], function(_) {
        return function(macroName) {
            var controls;

            function getMacroControls(allControls) {
                return _.first(_.filter(_.pluck(allControls, macroName), _.isObject));
            }
            return {
                getControls: function(callback) {
                    controls = controls ||
                        getMacroControls(WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-macro.property-panel-controls"));
                    callback(controls);
                }
            };
        };
    });
})(AJS.$, define);
