/**
 * Utility methods for rendering connect addons in AUI components
 */
_AP.define("host/content", ["_dollar", "_uri", "_ui-params"], function ($, uri, UiParams) {
    "use strict";

    function getContentUrl(pluginKey, capability){
        return AJS.contextPath() + "/plugins/servlet/ac/" + encodeURIComponent(pluginKey) + "/" + encodeURIComponent(capability.key);
    }

    function getWebItemPluginKey(target){
        var m = target.attr('class').match(/ap-plugin-key-([^\s]*)/);
        return $.isArray(m) ? m[1] : false;
    }
    function getWebItemModuleKey(target){
        var m = target.attr('class').match(/ap-module-key-([^\s]*)/);
        return $.isArray(m) ? m[1] : false;
    }

    function getOptionsForWebItem(target){
        var pluginKey = getWebItemPluginKey(target),
            moduleKey = getWebItemModuleKey(target),
            type = target.hasClass('ap-inline-dialog') ? 'inlineDialog' : 'dialog';
            return window._AP[type + 'Options'][pluginKey + ':' + moduleKey] || {};
    }

    // Deprecated. This passes the raw url to ContextFreeIframePageServlet, which is vulnerable to spoofing.
    // Will be removed when XML descriptors are dropped - plugins should pass key of the <dialog-page>, NOT the url.
    // TODO: Remove this class when support for XML Descriptors goes away
    function getIframeHtmlForUrl(pluginKey, remoteUrl, params) {
        var contentUrl = AJS.contextPath() + "/plugins/servlet/render-signed-iframe";
        return $.ajax(contentUrl, {
            dataType: "html",
            data: {
                "dialog": true,
                "ui-params": UiParams.encode(params),
                "plugin-key": pluginKey,
                "remote-url": remoteUrl,
                "width": "100%",
                "height": "100%",
                "raw": "true"
            }
        });
    }


    function getIframeHtmlForKey(pluginKey, productContextJson, capability, params) {
        var contentUrl = getContentUrl(pluginKey, capability);
        return $.ajax(contentUrl, {
            dataType: "html",
            data: {
                "ui-params": UiParams.encode(params),
                "plugin-key": pluginKey,
                "product-context": productContextJson,
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
                cp:     url.getQueryParamValue('cp')
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
        getOptionsForWebItem: getOptionsForWebItem
    };


});
