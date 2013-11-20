/**
 * Utility methods for rendering connect addons in AUI components
 */
_AP.define("host/content", ["_dollar"], function ($) {
    "use strict";

    function getIframeHtmlForKey(pluginKey, productContextJson, options) {
        var contentUrl = AJS.contextPath() + "/plugins/servlet/atlassian-connect/" + pluginKey + "/" + options.key;
        return $.ajax(contentUrl, {
            dataType: "html",
            data: {
                "dialog": true,
                "plugin-key": pluginKey,
                "product-context": productContextJson,
                "key": options.key,
                "width": "100%",
                "height": "100%"
            }
        });
    }


    function eventHandler(action, selector, callback) {

        function domEventHandler(event) {
            event.preventDefault();
            var $el = $(event.target);
            var href = $el.attr("href") || $el.parents("a").attr("href");
            var options = {bindTo: $el};
            var re = /[?&](width|height)=([^&]+)/g, match;
            while (match = re.exec(href)) {
                options[match[1]] = decodeURIComponent(match[2]);
            }

            callback(href, options);
        }

        // jquery 1.7 or later
        if ($().on) {
          // Connect any web items to the dialog.  Necessary to bind to dynamic action cogs in JIRA
            $(window.document).on("click", selector, domEventHandler);
        } else {
            // Bind to all static links
            var $webItems = $(selector);
            $webItems.each(function (index, el) {
                var $el = $(el);
                $el[action](domEventHandler);
            });
        }

    }

    return {
        getIframeHtmlForKey: getIframeHtmlForKey,
        eventHandler: eventHandler
    };


});
