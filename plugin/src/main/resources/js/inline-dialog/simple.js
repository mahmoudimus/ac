_AP.define("inline-dialog/simple", ["_dollar", "host/content"], function($, hostContentUtilities) {

    var idSeq = 0;

    var servletPlaceHolder = "ap-servlet-placeholder";

    return function (contentUrl, options) {
        var $nexus;

        var timeout = setTimeout(function () {
            $nexus
                .append("<div class='ap-inline-dialog-loading hidden'>&nbsp;</div>")
                .find(".ap-inline-dialog-loading").show();
        }, 500);

        function preventTimeout() {
            if (timeout) {
                clearTimeout(timeout);
                timeout = null;
            }
        }

        var $inlineDialog;

        var displayInlineDialog = function(content, trigger, showPopup) {
            populateInlineDialog(content);
            showPopup();

            $.ajax(contentUrl, {
                dataType: "html",
                success: function(data) {
                    preventTimeout();
                    content.html(data);
                    content.data('inlineDialog', $inlineDialog);
                },
                error: function(xhr, status, ex) {
                    preventTimeout();
                    var title = "Unable to load add-on content";
                    $nexus.html("<div class='aui-message error' style='margin: 10px'></div>");
                    $nexus.find(".error").append("<p class='title'>" + title + "</p>");
                    var msg = status + (ex ? ": " + ex.toString() : "");
                    $nexus.find(".error").append(msg);
                    AJS.log(msg);
                }
            });
            return false;
        };

        //Create the AUI inline dialog with a unique ID.
        $inlineDialog = AJS.InlineDialog(
            options.bindTo,
            //assign unique id to inline Dialog
            "ap-inline-dialog-" + (idSeq += 1),
            displayInlineDialog,
            {
                width: options.width
            });

        function populateInlineDialog(content){
            if($("." + servletPlaceHolder, content).length !== 1){
                content.wrapInner('<span class="' + servletPlaceHolder + '"></span>');
            }
            $nexus = content.find("." + servletPlaceHolder);
        }

        return {
            id: $inlineDialog.attr('id'),
            show: function() {
                $inlineDialog.show();
            },

        };
    };

});

