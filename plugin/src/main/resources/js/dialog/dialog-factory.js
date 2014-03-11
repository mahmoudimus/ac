_AP.define("dialog/dialog-factory", ["_dollar", "dialog/simple-dialog"], function($, dialog) {
    return function(options) {
        options.dlg = true;
        //options.chrome = true;
        return new dialog(options);
    }
});
