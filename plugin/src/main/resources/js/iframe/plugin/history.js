AP.define("history", ["_dollar", "_rpc", "_ui-params"],

/**
* History API
* Changing the URL of the host product, allowing manipulation of the browser history.
* Note: This is only enabled for page modules (Admin page, General page, Configure page, User profile page).
* ### Example ###
* ```
* AP.require(["history"], function(history){
*
*    // Register a function to run when state is changed.
*    // You should use this to update your UI to show the state.
*    history.popState(function(e){
*        alert("The URL has changed from: " + e.oldURL + "to: " + e.newURL);
*    });
*
*    // Adds a new entry to the history and changes the url in the browser.
*    history.pushState("page2");
*
*    // Changes the URL back and invokes any registered popState callbacks.
*    history.back();
*
* });
* ```
* @exports history
*/

function ($, rpc, uiParams) {
    "use strict";

    var popStateCallbacks = [];
    var state = uiParams.fromWindowName(null, "historyState");
    return rpc.extend(function (remote) {
        var exports = {
            /**
            * The current url anchor.
            * @return String
            * @example
            * AP.require(["history"], function(history){
            *    history.pushState("page5");
            *    history.getState(); // returns "page5";
            * });
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
            * @example
            * AP.require(["history"], function(history){
            *    history.go(-2); // go back by 2 entries in the browser history.
            * });
            */
            go: function(delta){
                remote.historyGo(delta);
            },
            /**
            * Goes back one step in the joint session history.
            * Will invoke the popstate callback
            * @example
            * AP.require(["history"], function(history){
            *    history.back(); // go back by 1 entry in the browser history.
            * });
            */
            back: function(){
                return this.go(-1);
            },
            /**
            * Goes back one step in the joint session history.
            * Will invoke the popstate callback
            * @example
            * AP.require(["history"], function(history){
            *    history.forward(); // go forward by 1 entry in the browser history.
            * });
            */
            forward: function(){
                return this.go(1);
            },
            /**
            * Pushes the given data onto the session history.
            * Does NOT invoke popState callback
            * @param String url to add to history
            */
            pushState: function(url){
                state = url;
                remote.historyPushState(url);
            },
            /**
            * Updates the current entry in the session history.
            * Does NOT invoke popState callback
            * @param String url to add to history
            */
            replaceState: function(url){
                state = url;
                remote.historyReplaceState(url);
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
                        try {
                            popStateCallbacks[i](e);
                        } catch (err) {
                            $.log("History popstate callback exception: " + err.message);
                        }
                    }
                }
            },
            stubs: ["historyPushState", "historyGo", "historyReplaceState"]
        };

    });

});
