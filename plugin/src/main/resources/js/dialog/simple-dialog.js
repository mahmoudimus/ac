_AP.define("dialog/simple-dialog", ["_dollar", "_uri", "host/_status_helper"], function($, uri, statusHelper) {

    var enc = encodeURIComponent;
    var $global = $(window);
    var idSeq = 0;
    var $nexus;
    var dialog;
    var dialogId;

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
            titleId: options.titleId,
            size: options.size,
            extraClasses: ['ap-aui-dialog2'],
            removeOnHide: true
        }));
        $el.find('.aui-dialog2-header-close').click(closeDialog);
        $el.find('.aui-dialog2-content').append($nexus);
        return $el;
    }

    function displayDialogContent($container, options){
        $container.attr('id', 'ap-' + options.ns);
        $container.append('<div id="embedded-' + options.ns + '" class="ap-dialog-container" />');
        options.dlg = true; //REMOVE THIS
//          container.append(statusHelper.createStatusMessages());
        _AP.create(options);
    }


    function parseDimension(value, viewport) {
        if (typeof value === "string") {
            var percent = value.indexOf("%") === value.length - 1;
            value = parseInt(value, 10);
            if (percent) value = value / 100 * viewport;
        }
        return value;
    }

    function setDimension(url, name, value) {
        name = enc(name);
        if (url.indexOf(name + "=")) {
            url = url.replace(new RegExp(name + "=[^&]+"), function () {
                return name + "=" + enc(value);
            });
        }
        else {
            url += "&" + name + "=" + enc(value);
        }
        return url;
    }


    function closeDialog() {
        if ($nexus) {
            // Signal the XdmRpc for the dialog's iframe to clean up
            $nexus.trigger("ra.iframe.destroy");
            // Clear the nexus handle to allow subsequent dialogs to open
            $nexus = null;
        }
        dialog.hide();
    }

    return {
        id: dialogId,

        /**
        * Constructs a new AUI dialog wrapper for a Remotable Plugin. The dialog has a single content panel containing a single
        * iframe. The iframe's content is retrieved from the Remotable Plugin via a redirect URl from the host Atlassian app,
        * which the request to the Remotable Plugin to be signed with outgoing OAuth credentials.
        *
        * @param {String} contentUrl The URL (relative to the Atlassian app root) that will retrieve the content to display,
        *           eg. "/plugins/servlet/atlassian-connect/app-key/macro".
        * @param {Object} options Options to configure the behaviour and appearance of the dialog.
        * @param {String} [options.header="Remotable Plugins Dialog Title"]  Dialog header.
        * @param {String} [options.headerClass="ap-dialog-header"] CSS class to apply to dialog header.
        * @param {String|Number} [options.width="50%"] width of the dialog, expressed as either absolute pixels (eg 800) or percent (eg 50%)
        * @param {String|Number} [options.height="50%"] height of the dialog, expressed as either absolute pixels (eg 600) or percent (eg 50%)
        * @param {String} [options.id] ID attribute to assign to the dialog. Default to "ap-dialog-n" where n is an autoincrementing id.
        */
        create: function(options) {

            var hasClosed = false,
                defaultOptions = {
                    // These options really _should_ be provided by the caller, or else the dialog is pretty pointless
                    width: "50%",
                    height: "50%"
                },
                dialogId = options.id || "ap-dialog-" + (idSeq += 1),
                mergedOptions = $.extend({id: dialogId}, defaultOptions, options),
                dialogElement;

            mergedOptions.w = parseDimension(mergedOptions.width, $global.width());
            mergedOptions.h = parseDimension(mergedOptions.height, $global.height());

            if(options.size){
                mergedOptions.w = "100%";
                mergedOptions.h = "100%";
            }

            console.log("CHROME", options.chrome);
            $nexus = $("<div />").addClass("ap-servlet-placeholder ap-dialog-container");

            if(options.chrome){
                dialogElement = createDialogElement(mergedOptions, $nexus);

            } else {
                dialogElement = createChromelessDialogElement(mergedOptions, $nexus);
            }

            dialog = AJS.dialog2(dialogElement);
            displayDialogContent($nexus, mergedOptions);
            dialog.show();

        },
        close: closeDialog
    };

});

