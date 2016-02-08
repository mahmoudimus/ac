(function($, define){

    define("ac/confluence/macro/property-panel-controls", [], function() {
        return function(addonName, macroName) {
            return {
                getControls: function(callback) {
                    $.ajax(AJS.contextPath() + '/rest/atlassian-connect/1/controls/' + addonName + '/' + macroName + '/property-panel').done(function(response)
                    {
                        var buttonModel = {};
                        var response = JSON.parse(response);
                        if (response.length == 1 && response[0].type === "button")
                        {
                            buttonModel.className = "macro-placeholder-property-panel-hello-button";
                            buttonModel.text = response[0].displayName;
                            callback([buttonModel]);
                        }
                        else
                        {
                            callback([]);
                        }
                    });
                }
            };
        };
    });
})(AJS.$, define);
