_AP.define("dialog/aui-dialog", ["_dollar", "_uri", "dialog/simple-dialog"], function($, uri, simpleDialog) {
    var base = {
        createDialogElement: function(options){
            alert('i am creating the dialog element');
            // TODO: copied from AUI dialog2 soy. Should make it use that when it's in products.
            var $el = AJS.$("<section></section>")
              .addClass("aui-layer aui-layer-hidden aui-layer-modal")
              .addClass("aui-dialog2 aui-dialog2-" + (options.size || "medium"))
              .addClass("ap-aui-dialog2")
              .attr("role", "dialog")
              .attr("data-aui-blanketed", "true")
              .attr("data-aui-focus-selector", ".aui-dialog2-content :input:visible:enabled");

              $el.attr("id", options.id);

            if (options.titleId) {
              $el.attr("aria-labelledby", options.titleId);
            }
            return $el;
        },
        displayDialogContent: function($container, options){
            $container.attr('id', 'ap-' + options.ns);
            $container.append('<div id="embedded-' + options.ns + '" class="ap-dialog-container" />');
            options.dlg = true; //REMOVE THIS
//          container.append(statusHelper.createStatusMessages());
            _AP.create(options);
        }
    };

    return function (options) {
        return simpleDialog(options, base);
    };

});

