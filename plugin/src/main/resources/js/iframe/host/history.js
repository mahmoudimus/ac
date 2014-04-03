/**
 * Methods for showing the status of a connect-addon (loading, time'd-out etc)
 */

_AP.define("host/history", ["_dollar", "_uri"], function ($, Uri) {
    "use strict";

    var stateStorage = [];

    var anchorPrefix = "!";

    function stripPrefix (text) {
        return text.replace(new RegExp("^" + anchorPrefix), "");
    }

    function addPrefix (text) {
        return anchorPrefix + stripPrefix(text);
    }


    function changeState (data, anchor, replace) {
        var newUrlObj = new Uri.init(window.location.href);
        newUrlObj.anchor(addPrefix(anchor));
        //newUrlObj.anchor
        var currentUrlObj = new Uri.init(window.location.href);
        // If the url has changed.
        if(newUrlObj.anchor() !== currentUrlObj.anchor()){
            stateStorage[newUrlObj.toString()] = data;

            if(replace){
                window.location.replace("#" + newUrlObj.anchor());
            } else {
                window.location.assign("#" + newUrlObj.anchor());
            }
        }
    }

    function pushState (data, title, url) {
        // TODO add title support
        changeState(data, url);
    }

    function replaceState (data, title, url) {
        // TODO add title support.
        changeState(data, url, true);
    }

    function go (delta) {
        history.go(delta);
    }


    function hashChange(event, rpcCallback){
        console.log('hashchange', event);
        var newUrlObj = new Uri.init(event.newURL);
        var oldUrlObj = new Uri.init(event.oldURL);
        console.log(stateStorage);
        if(newUrlObj.anchor() !== oldUrlObj.anchor()){
            console.log('call rpc');
            rpcCallback(sanitizeHashChangeEvent(event));
        }
    }

    function sanitizeHashChangeEvent(e){
        return {
            newURL: stripPrefix(new Uri.init(e.newURL).anchor()),
            oldURL: stripPrefix(new Uri.init(e.newURL).anchor())
        };
    }

    return {
        pushState: pushState,
        replaceState: replaceState,
        go: go,
        hashChange: hashChange
    };

});
