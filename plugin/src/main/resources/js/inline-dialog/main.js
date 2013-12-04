_AP.define("inline-dialog", ["_dollar"], function($) {

    function getInlineDialog($content){
        return $content.closest('.contents').data('inlineDialog');
    }

    function showInlineDialog($content) {
        getInlineDialog($content).show();
    }

    function resizeInlineDialog($content, width, height) {
        $content.closest('.contents').css({width: width, height: height});
        refreshInlineDialog($content);
    }

    function refreshInlineDialog($content) {
        getInlineDialog($content).refresh();
    }

    return {
        showInlineDialog: showInlineDialog,
        resizeInlineDialog: resizeInlineDialog
    };

});
