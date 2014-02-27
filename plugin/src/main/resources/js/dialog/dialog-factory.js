_AP.define("dialog-factory", ["_dollar", "dialog/aui-dialog", "dialog/simple-dialog"], function($, auiDialog, simpleDialog) {
    return function(href, options) {
        var dialog = simpleDialog;

        //if dialog needs aui chrome. Extend the base object with aui methods.
        if(options.chrome) {
            $.extend(simpleDialog, auiDialog);
        }

        return dialog(href, options);
    }
});
