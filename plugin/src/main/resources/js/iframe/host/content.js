/**
 * Utility methods for rendering connect addons in AUI components
 */
_AP.define("host/content", ["_dollar", "_uri", "_ui-params"], function ($, uri, UiParams) {
    "use strict";

    function getContentUrl(pluginKey, capability){
        return AJS.contextPath() + "/plugins/servlet/ac/" + encodeURIComponent(pluginKey) + "/" + encodeURIComponent(capability.key);
    }

    function getWebItemPluginKey(target){
        var cssClass = target.attr('class');
        var m = cssClass ? cssClass.match(/ap-plugin-key-([^\s]*)/) : null;
        return $.isArray(m) ? m[1] : false;
    }
    function getWebItemModuleKey(target){
        var cssClass = target.attr('class');
        var m = cssClass ? cssClass.match(/ap-module-key-([^\s]*)/) : null;
        return $.isArray(m) ? m[1] : false;
    }

    function getOptionsForWebItem(target){
        var pluginKey = getWebItemPluginKey(target),
            moduleKey = getWebItemModuleKey(target),
            type = target.hasClass('ap-inline-dialog') ? 'inlineDialog' : 'dialog';
            return window._AP[type + 'Options'][moduleKey] || {};
    }

    // Deprecated. This passes the raw url to ContextFreeIframePageServlet, which is vulnerable to spoofing.
    // Will be removed when XML descriptors are dropped - plugins should pass key of the <dialog-page>, NOT the url.
    // TODO: Remove this class when support for XML Descriptors goes away
    function getIframeHtmlForUrl(pluginKey, remoteUrl, productContext, params) {
        var contentUrl = AJS.contextPath() + "/plugins/servlet/render-signed-iframe";
        return $.ajax(contentUrl, {
            dataType: "html",
            data: {
                "dialog": true,
                "ui-params": UiParams.encode(params),
                "plugin-key": pluginKey,
                "product-context": JSON.stringify(productContext),
                "remote-url": remoteUrl,
                "width": "100%",
                "height": "100%",
                "raw": "true"
            }
        });
    }

    function getIframeHtmlForKey(pluginKey, productContext, capability, uiParams, targetUri) {
        var contentUrl = getContentUrl(pluginKey, capability);

        if (targetUri) {
            contentUrl += targetUri.query(); // add "?page.id=1234" etc from the clicked link (will be permission checked by the iframe servlet)
        }

        return $.ajax(contentUrl, {
            dataType: "html",
            data: {
                "ui-params": UiParams.encode(uiParams),
                "plugin-key": pluginKey,
                "product-context": JSON.stringify(productContext),
                "key": capability.key,
                "width": "100%",
                "height": "100%",
                "raw": "true"
            }
        });
    }


    function eventHandler(action, selector, callback) {

        function domEventHandler(event) {
            event.preventDefault();
            var $el = $(event.target).closest(selector),
            href = $el.attr("href"),
            url = new uri.init(href),
            options = {
                bindTo: $el,
                header: $el.text(),
                width:  url.getQueryParamValue('width'),
                height: url.getQueryParamValue('height'),
                cp:     url.getQueryParamValue('cp'),
                key: getWebItemPluginKey($el)
            };
            callback(href, options, event.type);
        }

        $(window.document).on(action, selector, domEventHandler);

    }

    return {
        getContentUrl: getContentUrl,
        getIframeHtmlForUrl: getIframeHtmlForUrl,
        getIframeHtmlForKey: getIframeHtmlForKey,
        eventHandler: eventHandler,
        getOptionsForWebItem: getOptionsForWebItem,
        getWebItemPluginKey: getWebItemPluginKey,
        getWebItemModuleKey: getWebItemModuleKey
    };


});
