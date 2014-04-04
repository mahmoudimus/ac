AP.define("history", ["_dollar", "_rpc", "_ui-params"],

/**
* History API
* Mimmic the HTML5 history API by changing the URL of the host product instead of the iframe.
* Note: This is only enabled for page modules (Admin page, General page, Configure page, User profile page)
* ### Example ###
* ```
* AP.require(["history"], function(bridgeHistory){

    // Register a function to run when state is changed.
    // You should use this to update your UI to show the state.
    bridgeHistory.popState(function(e){
        alert("The URL has changed from: " + e.oldURL + "to: " + e.newURL);
    });

    // Adds a new entry to the history and changes the url in the browser.
    bridgeHistory.pushState({foo: "bar"}, "page 2", "page2");

    // Changes the URL back and invokes any registered popState callbacks.
    bridgeHistory.back();

});
* ```
* @exports messages
*/

function ($, rpc, uiParams) {
    "use strict";

    var popStateCallbacks = [];
    var state = uiParams.fromWindowName(null, "historyState");
    return rpc.extend(function (remote) {
        var exports = {
            /**
            * The state when the add-on loads
            */
            getState: function(){
                return state;
            },

            /**
            * Goes back or forward the specified number of steps
            * A zero delta will reload the current page.
            * If the delta is out of range, does nothing.
            * Will invoke the popstate callback
            * @param int delta
            */
            go: function(delta){
                remote.historyGo(delta);
            },
            /**
            * Goes back one step in the joint session history.
            * Will invoke the popstate callback
            */
            back: function(){
                return this.go(-1);
            },
            /**
            * Goes back one step in the joint session history.
            * Will invoke the popstate callback
            */
            forward: function(){
                return this.go(1);
            },
            /**
            * Pushes the given data onto the session history.
            * Does NOT invoke popState callback
            * @param String data
            * @param String title
            * @param String url optional
            */
            pushState: function(data, title, url){
                state = url;
                remote.historyPushState(data, title, url);
            },
            /**
            * Updates the current entry in the session history.
            * Does NOT invoke popState callback
            * @param String data
            * @param String title
            * @param String url optional
            */
            replaceState: function(data, title, url){
                remote.historyReplaceState(data, title, url);
            },
            /**
            * Register a function to be executed on state change
            * @param Function callback to be executed on state change.
            */
            popState: function(callback){
                popStateCallbacks.push(callback);
            }
        };

        return {
            apis: exports,
            internals: {
                historyMessage: function(e){
                    state = e.newURL;
                    for(var i in popStateCallbacks){
                        popStateCallbacks[i](e);
                    }
                }
            },
            stubs: ["historyPushState", "historyGo", "historyReplaceState"]
        };

    });

});
