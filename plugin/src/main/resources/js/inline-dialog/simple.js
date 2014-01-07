_AP.define("inline-dialog/simple", ["_dollar", "host/content"], function($, hostContentUtilities) {

    var idSeq = 0;

    return function (contentUrl, options) {
        var $inlineDialog;

        var displayInlineDialog = function(content, trigger, showPopup) {

            if(!options.ns){
                options.ns = 'inline-dialog-content-' + idSeq;
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
                content.append(hostContentUtilities.createStatusMessages());
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
            "ap-inline-dialog-" + idSeq,
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
