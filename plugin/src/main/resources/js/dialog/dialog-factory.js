_AP.define("dialog-factory", ["_dollar", "dialog/aui-dialog", "dialog/simple-dialog"], function($, auiDialog, simpleDialog) {
    return function(href, options) {
        var dialog = simpleDialog;

        //if dialog needs aui chrome. return an aui dialog instead
        if(options.chrome) {
            dialog = auiDialog;
        }

        return dialog(href, options);
    }
});
