/**
 * Utility methods for rendering connect addons in AUI components
 */
_AP.define("host/content", ["_dollar"], function ($) {
    "use strict";

    function getContentUrl(pluginKey, capability){
        return AJS.contextPath() + "/plugins/servlet/atlassian-connect/" + pluginKey + "/" + capability.key;
    }

    function createStatusMessages() {
        var i,
        stats = $('<div class="ap-stats" />'),
        statuses = {
            loading: {
                description: 'Loading add-on...'
            },
            "load-timeout": {
                description: 'Add-on is not responding. Wait or <a href="#" class="ap-btn-cancel">cancel</a>?'
            },
            "load-error": {
                description: 'Add-on failed to load.'
            }
        };

        for(i in statuses){
            var status = $('<div class="ap-' + i + ' ap-status hidden" />');
            status.append('<small>' + statuses[i].description + '</small>');
            stats.append(status);
        }
        return stats;

    }

    function getIframeHtmlForKey(pluginKey, productContextJson, capability) {
        var contentUrl = this.getContentUrl(pluginKey, capability);
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
        getContentUrl: getContentUrl,
        getIframeHtmlForKey: getIframeHtmlForKey,
        eventHandler: eventHandler,
        createStatusMessages: createStatusMessages
    };


});
