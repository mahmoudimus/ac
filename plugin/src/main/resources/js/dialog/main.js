_AP.define("dialog/main", ["_dollar", "_uri", "host/_status_helper", "dialog/button"], function($, uri, statusHelper, dialogButton) {

    var $global = $(window);
    var idSeq = 0;
    var $nexus;
    var dialog;
    var dialogId;

    var buttons = {
        submit: dialogButton.submit({
            done: closeDialog
        }),
        cancel: dialogButton.cancel({
            done: closeDialog
        })
    };

    function createChromelessDialogElement(options, $nexus){
        var $el = $(aui.dialog.dialog2Chrome({
            id: options.id,
            titleId: options.titleId,
            size: options.size,
            extraClasses: ['ap-aui-dialog2'],
            removeOnHide: true
        }));
        $el.append($nexus);
        return $el;
    }

    function createDialogElement(options, $nexus){
        var $el = $(aui.dialog.dialog2({
            id: options.id,
            titleText: options.header,
            titleId: options.titleId,
            size: options.size,
            extraClasses: ['ap-aui-dialog2'],
            removeOnHide: true,
            footerActionContent: true
        }));

        buttons.submit.setText(options.submitText);
        buttons.cancel.setText(options.cancelText);

        //soy templates don't support sending objects, so make the template and bind them.
        $el.find('.aui-dialog2-footer-actions').empty().append(buttons.submit.$el, buttons.cancel.$el);

        $el.find('.aui-dialog2-content').append($nexus);
        $nexus.data('ra.dialog.buttons', buttons);

        function handler(button) {
            // ignore clicks on disabled links
            if(button.isEnabled()){
                button.$el.trigger("ra.dialog.click", button.dispatch);
            }
        }

        $.each(buttons, function(i, button) {
            button.$el.click(function(){
                handler(button);
            });
        });

        return $el;
    }

    function displayDialogContent($container, options){
        $container.append('<div id="embedded-' + options.ns + '" class="ap-dialog-container" />');
    }


    function parseDimension(value, viewport) {
        if (typeof value === "string") {
            var percent = value.indexOf("%") === value.length - 1;
            value = parseInt(value, 10);
            if (percent) value = value / 100 * viewport;
        }
        return value;
    }

    function closeDialog() {
        if ($nexus) {
            // Signal the XdmRpc for the dialog's iframe to clean up
            $nexus.trigger("ra.iframe.destroy")
            .removeData("ra.dialog.buttons")
            .unbind();
            // Clear the nexus handle to allow subsequent dialogs to open
            $nexus = null;
        }
        dialog.hide();
    }

    return {
        id: dialogId,

        /**
        * Constructs a new AUI dialog. The dialog has a single content panel containing a single iframe.
        * The iframe's content is either created by loading [options.src] as the iframe url. Or fetching the content from the server by add-on key + module key.
        *
        * @param {Object} options Options to configure the behaviour and appearance of the dialog.
        * @param {String} [options.header="Remotable Plugins Dialog Title"]  Dialog header.
        * @param {String} [options.headerClass="ap-dialog-header"] CSS class to apply to dialog header.
        * @param {String|Number} [options.width="50%"] width of the dialog, expressed as either absolute pixels (eg 800) or percent (eg 50%)
        * @param {String|Number} [options.height="50%"] height of the dialog, expressed as either absolute pixels (eg 600) or percent (eg 50%)
        * @param {String} [options.id] ID attribute to assign to the dialog. Default to "ap-dialog-n" where n is an autoincrementing id.
        */
        create: function(options, showLoadingIndicator) {

            var defaultOptions = {
                    // These options really _should_ be provided by the caller, or else the dialog is pretty pointless
                    width: "50%",
                    height: "50%"
                },
                dialogId = options.id || "ap-dialog-" + (idSeq += 1),
                mergedOptions = $.extend({id: dialogId}, defaultOptions, options, {dlg: 1}),
                dialogElement;

            mergedOptions.w = parseDimension(mergedOptions.width, $global.width());
            mergedOptions.h = parseDimension(mergedOptions.height, $global.height());

            $nexus = $("<div />").addClass("ap-servlet-placeholder").attr('id', 'ap-' + options.ns)
            .bind("ra.dialog.close", closeDialog);

            if(options.chrome){
                dialogElement = createDialogElement(mergedOptions, $nexus);

            } else {
                dialogElement = createChromelessDialogElement(mergedOptions, $nexus);
            }

            if(options.size){
                mergedOptions.w = "100%";
                mergedOptions.h = "100%";
            } else {
                AJS.layer(dialogElement).changeSize(mergedOptions.w, mergedOptions.h);
            }

            dialog = AJS.dialog2(dialogElement);
            dialog.on("hide", function() {
                closeDialog();
            });

            displayDialogContent($nexus, mergedOptions);

            if(showLoadingIndicator !== false){
                $nexus.append(statusHelper.createStatusMessages());
            }

            //difference between a webitem and opening from js.
            if(options.src){
                _AP.create(mergedOptions);
            }

            dialog.show();

        },
        close: closeDialog
    };

});

