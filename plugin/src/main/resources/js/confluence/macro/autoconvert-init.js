(function($, require){
    require(["ac/confluence/macro/autoconvert"], function(Autoconvert) {
        AJS.bind("init.rte", function() {
            var autoconvertDefs = WRM.data.claim("com.atlassian.plugins.atlassian-connect-plugin:confluence-atlassian-connect-autoconvert-resources.connect-autoconvert-data");
            Autoconvert.registerAutoconvertHandlers(autoconvertDefs,tinymce)
        });
    });
})(AJS.$, require);