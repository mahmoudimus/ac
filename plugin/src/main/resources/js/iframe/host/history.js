/**
 * Methods for showing the status of a connect-addon (loading, time'd-out etc)
 */

_AP.define("host/history", ["_dollar", "_uri"], function ($, Uri) {
    "use strict";

    var stateStorage = {
        historyStack: [],
        add: function(key, data, dontstrip){
            if(dontstrip !== true){
                key = stripPrefix(key);
            }

            this.historyStack.push({
                page: key,
                data: data
            });
        },
        last: function(){
            return this.historyStack[this.historyStack.length - 1];
        }
    };

    var anchorPrefix = "!";

    function stripPrefix (text) {
        return text.toString().replace(new RegExp("^" + anchorPrefix), "");
    }

    function addPrefix (text) {
        return anchorPrefix + stripPrefix(text);
    }


    function changeState (data, anchor, replace) {
        var newUrlObj = new Uri.init(window.location.href);
        newUrlObj.anchor(addPrefix(anchor));
        // newUrlObj.anchor
        var currentUrlObj = new Uri.init(window.location.href);
        // If the url has changed.
        if(newUrlObj.anchor() !== currentUrlObj.anchor()){
            stateStorage.add(newUrlObj.anchor(), data);
            // If it was replaceState or pushState?
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
        var newUrlObj = new Uri.init(event.newURL);
        var oldUrlObj = new Uri.init(event.oldURL);
        if(
            ( newUrlObj.anchor() !== oldUrlObj.anchor() ) && // if the url has changed
            ( stateStorage.last().page !== undefined ) && // and the url has changed before
            ( stateStorage.last().page !== stripPrefix(newUrlObj.anchor()) ) //  and it's not the page we just pushed.
         ){
            rpcCallback(sanitizeHashChangeEvent(event));
        }
    }

    function sanitizeHashChangeEvent(e){
        return {
            newURL: stripPrefix(new Uri.init(e.newURL).anchor()),
            oldURL: stripPrefix(new Uri.init(e.oldURL).anchor())
        };
    }

    return {
        pushState: pushState,
        replaceState: replaceState,
        go: go,
        hashChange: hashChange
    };

});
