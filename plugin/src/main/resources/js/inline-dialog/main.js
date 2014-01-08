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

    /**
    * init
    * sets up an inline dialog.
    * @param {String} id            The id of the link / button that the dialog binds to.
    * @param {Object} properties    A list of properties. see: https://extranet.atlassian.com/display/ARA/Product%3A+Inline+Dialog
    */
    function init(id, properties){
        properties = properties || {};

        AJS.toInit(function ($) {
            properties.bindTo = $('#' + id);
            var dialog = simpleInlineDialog(properties.bindTo.attr("href"), properties);

            $(window.document).on("click", properties.bindTo.attr('id'), dialog.show);
        });
    }

    return {
        showInlineDialog: showInlineDialog,
        resizeInlineDialog: resizeInlineDialog,
        init: init
    };

});
