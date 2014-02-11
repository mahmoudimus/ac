/**
 * Utility methods for rendering connect addons in AUI components
 */
_AP.define("host/content", ["_dollar", "_uri"], function ($, uri) {
    "use strict";

    function getContentUrl(pluginKey, capability){
        return AJS.contextPath() + "/plugins/servlet/ac/" + encodeURIComponent(pluginKey) + "/" + encodeURIComponent(capability.key);
    }

    function getWebItemPluginKey(target){
        return target.attr('class').match(/ap-plugin-key-[^\s]*/)[0];
    }
    function getWebItemModuleKey(target){
        return target.attr('class').match(/ap-module-key-[^\s]*/)[0];
    }

    function getOptionsForWebItem(target){
        var pluginKey = getWebItemPluginKey(target),
            moduleKey = getWebItemModuleKey(target),
            type = target.hasClass('ap-inline-dialog') ? 'inlineDialog' : 'dialog';

        return window._AP[type + 'Options'][pluginKey + ':' + moduleKey] || {};
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
                "height": "100%",
                "raw": "true"
            }
        });
    }


    function eventHandler(action, selector, callback) {

        function domEventHandler(event) {
            event.preventDefault();
            var $el = $(event.target),
            href = $el.closest("a").attr("href"),
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
        getIframeHtmlForKey: getIframeHtmlForKey,
        eventHandler: eventHandler,
        getOptionsForWebItem: getOptionsForWebItem,

    };


});
