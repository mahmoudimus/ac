/**
 * Methods for showing the status of a connect-addon (loading, time'd-out etc)
 */

_AP.define("host/history", ["_dollar", "_uri", "_rpc"], function ($, Uri, rpc) {
    "use strict";
    var lastAdded,
        anchorPrefix = "!";

    function stripPrefix (text) {
        if(text === undefined || text === null){
            return "";
        }
        return text.toString().replace(new RegExp("^" + anchorPrefix), "");
    }

    function addPrefix (text) {
        if(text === undefined || text === null){
            throw "You must supply text to prefix";
        }

        return anchorPrefix + stripPrefix(text);
    }

    function changeState (anchor, replace) {
        var currentUrlObj = new Uri.init(window.location.href),
        newUrlObj = new Uri.init(window.location.href);

        newUrlObj.anchor(addPrefix(anchor));

        // If the url has changed.
        if(newUrlObj.anchor() !== currentUrlObj.anchor()){
            lastAdded = newUrlObj.anchor();
            // If it was replaceState or pushState?
            if(replace){
                window.location.replace("#" + newUrlObj.anchor());
            } else {
                window.location.assign("#" + newUrlObj.anchor());
            }
            return newUrlObj.anchor();
        }
    }

    function pushState (url) {
        changeState(url);
    }

    function replaceState (url) {
        changeState(url, true);
    }

    function go (delta) {
        history.go(delta);
    }

    function hashChange (event, historyMessage) {
        var newUrlObj = new Uri.init(event.newURL);
        var oldUrlObj = new Uri.init(event.oldURL);
        if( ( newUrlObj.anchor() !== oldUrlObj.anchor() ) && // if the url has changed
            ( lastAdded !== newUrlObj.anchor() ) //  and it's not the page we just pushed.
         ){
            historyMessage(sanitizeHashChangeEvent(event));
        }
        lastAdded = null;
    }

    function sanitizeHashChangeEvent (e) {
        return {
            newURL: stripPrefix(new Uri.init(e.newURL).anchor()),
            oldURL: stripPrefix(new Uri.init(e.oldURL).anchor())
        };
    }

    function getState () {
        var hostWindowUrl = new Uri.init(window.location.href),
        anchor = stripPrefix(hostWindowUrl.anchor());
        return anchor;
    }

    rpc.extend(function(config){
        return {
            init: function (state) {
                if(state.uiParams.isGeneral){
                    // register for url hash changes to invoking history.popstate callbacks.
                    $(window).on("hashchange", function(e){
                        hashChange(e.originalEvent, state.historyMessage);
                    });
                }
            },
            internals: {
                historyPushState: function (url) {
                    if(this.uiParams.isGeneral){
                        return pushState(url);
                    } else {
                        $.log("History is only available to page modules");
                    }
                },
                historyReplaceState: function (url) {
                    if(this.uiParams.isGeneral){
                        return replaceState(url);
                    } else {
                        $.log("History is only available to page modules");
                    }
                },
                historyGo: function (delta) {
                    if(this.uiParams.isGeneral){
                        return go(delta);
                    } else {
                        log("History is only available to page modules");
                    }
                }
            },
            stubs: ["historyMessage"] 
        };
    });

});
