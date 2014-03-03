_AP.define("dialog/dialog-factory", ["_dollar", "dialog/aui-dialog", "dialog/simple-dialog"], function($, auiDialog, simpleDialog) {
    return function(options) {
        var dialog = simpleDialog;

        //if dialog needs aui chrome. return an aui dialog instead
        if(options.chrome) {
            dialog = auiDialog;
        }

        options.dlg = true;
        return new dialog(options);
    }
});
