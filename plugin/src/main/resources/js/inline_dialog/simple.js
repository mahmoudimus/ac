_AP.define("inline_dialog/simple", ["_dollar", "host/content"], function($, hostContentUtilities) {

    var idSeq = 0;

    return function (contentUrl, options) {
        var $nexus;

        var timeout = setTimeout(function () {
            $nexus
                .append("<div class='ap-inline-dialog-loading hidden'>&nbsp;</div>")
                .find(".ap-inline-dialog-loading").height($panelBody.height()).fadeIn();
        }, 500);

        function preventTimeout() {
            if (timeout) {
                clearTimeout(timeout);
                timeout = null;
            }
        }

        var displayInlineDialog = function(content, trigger, showPopup) {

            var $apContent = $(options.bindTo).siblings('.ap-content');
            populateInlineDialog(content);
            showPopup();

            $.ajax(contentUrl, {
              dataType: "html",
              success: function(data) {
                preventTimeout();
                content.html(data);
              },
              error: function(xhr, status, ex) {
                preventTimeout();
                var title = "Unable to load plugin content.  Please try again later.";
                $nexus.html("<div class='aui-message error' style='margin: 10px'></div>");
                $nexus.find(".error").append("<p class='title'>" + title + "</p>");
                var msg = status + (ex ? ": " + ex.toString() : "");
                $nexus.find(".error").append(msg);
                AJS.log(msg);
              }
            });
            return false;
        };

        var $inlineDialog = AJS.InlineDialog(
            options.bindTo,
            "ap-inline-dialog-" + (idSeq += 1),
            displayInlineDialog,
            {
                width: options.width
            });


        var hasClosed = false;
        function closeInlineDialog() {
            if (hasClosed) return;
            $nexus
                .trigger("ra.iframe.destroy")
                .unbind();
            inlineDialog.remove();
            hasClosed = true;
        }

        function populateInlineDialog(content){
            if($(".ap-servlet-placeholder", content).length !== 1){
                content.wrapInner('<span class="ap-servlet-placeholder"></span>');
            }
            $nexus = content.find(".ap-servlet-placeholder");
        }

        return {
            id: $inlineDialog.attr('id'),
            show: function() {
                $inlineDialog.show();
            },
            close: closeInlineDialog,
            hide: function() {
                $inlineDialog.hide();
            }
        };
    };

});

