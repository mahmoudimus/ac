_AP.define("inline-dialog", ["_dollar"], function($) {

    function getInlineDialog($content){
        return $content.parents('.contents').data('inlineDialog');
    }

    function showInlineDialog($content) {
        getInlineDialog($content).show();
    }

    return {
        showInlineDialog: showInlineDialog
    };

});
