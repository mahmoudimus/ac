/**
 * Methods for showing the status of a connect-addon (loading, time'd-out etc)
 */

_AP.define("host/history", ["_dollar", "_uri"], function ($, Uri) {
    "use strict";

    var lastAdded,
        anchorPrefix = "!";

    function stripPrefix (text) {
        return text.toString().replace(new RegExp("^" + anchorPrefix), "");
    }

    function addPrefix (text) {
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

    function hashChange (event, rpcCallback) {
        var newUrlObj = new Uri.init(event.newURL);
        var oldUrlObj = new Uri.init(event.oldURL);
        if( ( newUrlObj.anchor() !== oldUrlObj.anchor() ) && // if the url has changed
            ( lastAdded !== newUrlObj.anchor() ) //  and it's not the page we just pushed.
         ){
            rpcCallback(sanitizeHashChangeEvent(event));
        }
        lastAdded = null;
    }

    function sanitizeHashChangeEvent (e) {
        return {
            newURL: stripPrefix(new Uri.init(e.newURL).anchor()),
            oldURL: stripPrefix(new Uri.init(e.oldURL).anchor())
        };
    }

    function getInitialIframeState () {
        var hostWindowUrl = new Uri.init(window.location.href),
        anchor = stripPrefix(hostWindowUrl.anchor());
        return anchor;
    }

    return {
        pushState: pushState,
        replaceState: replaceState,
        go: go,
        hashChange: hashChange,
        getInitialIframeState: getInitialIframeState
    };

});
