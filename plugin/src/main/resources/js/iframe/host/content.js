/**
 * Utility methods for rendering connect addons in AUI components
 */
_AP.define("host/content", ["_dollar"], function ($) {
    "use strict";

    function getIframeHtmlForKey(pluginKey, productContextJson, capability) {
        var contentUrl = AJS.contextPath() + "/plugins/servlet/atlassian-connect/" + pluginKey + "/" + capability.key;
        return $.ajax(contentUrl, {
            dataType: "html",
            data: {
                "dialog": true,
                "plugin-key": pluginKey,
                "product-context": productContextJson,
                "key": capability.key,
                "width": "100%",
                "height": "100%"
            }
        });
    }


    function eventHandler(action, selector, callback) {

        function domEventHandler(event) {
            event.preventDefault();
            var $el = $(event.target);
            var href = $el.closest("a").attr("href");
            var options = {bindTo: $el};
            var re = /[?&](width|height)=([^&]+)/g, match;
            while (match = re.exec(href)) {
                options[match[1]] = decodeURIComponent(match[2]);
            }

            callback(href, options);
        }

        $(window.document).on("click", selector, domEventHandler);

    }

    return {
        getIframeHtmlForKey: getIframeHtmlForKey,
        eventHandler: eventHandler
    };


});
