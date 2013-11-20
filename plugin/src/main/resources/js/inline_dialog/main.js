_AP.define("inline-dialog", ["_dollar"], function($) {

    function hideInlineDialog($content) {
        var $el = $content.parents('.aui-inline-dialog');
        $el.hide();
    }

    function showInlineDialog($content) {
        var $el = $content.parents('.aui-inline-dialog');
        $el.show();
    }

    return {
        hideInlineDialog: hideInlineDialog,
        showInlineDialog: showInlineDialog
    };

});
