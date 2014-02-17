_AP.define("inline-dialog", ["_dollar", "inline-dialog/simple"], function($, simpleInlineDialog) {

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

    function hideInlineDialog($content){
        getInlineDialog($content).hide();
    }

    return {
        showInlineDialog: showInlineDialog,
        resizeInlineDialog: resizeInlineDialog,
        hideInlineDialog: hideInlineDialog
    };

});
