_AP.define("inline-dialog/simple", ["_dollar", "host/_status_helper"], function($, statusHelper) {

    var idSeq = 0;
    function nextId(){
        return 'inline-dialog-content-' + idSeq;
    }

    return function (contentUrl, options) {
        var $inlineDialog;

        var displayInlineDialog = function(content, trigger, showPopup) {

            if(!options.ns){
                options.ns = nextId();
            }
            if(!options.url){
                options.url = contentUrl;
            }
            options.container = options.ns;
            options.src = options.url;
            content.data('inlineDialog', $inlineDialog);
            if(!content.find('iframe').length){
                content.attr('id', 'ap-' + options.ns);
                content.append('<div id="embedded-' + options.ns + '" />');
                content.append(statusHelper.createStatusMessages());
                _AP.create(options);
            }
            showPopup();
            return false;
        };

        idSeq++;

        //Create the AUI inline dialog with a unique ID.
        $inlineDialog = AJS.InlineDialog(
            options.bindTo,
            //assign unique id to inline Dialog
            "ap-" + nextId(),
            displayInlineDialog,
            {
                width: options.width
            });

        return {
            id: $inlineDialog.attr('id'),
            show: function() {
                $inlineDialog.show();
            },

        };
    };

});
