AP.define("history", ["_dollar", "_rpc"],

/**
* History API
* ```
* ```
* @exports messages
*/

function ($, rpc) {
    "use strict";

    return rpc.extend(function (remote) {
        var popState = [];
        return {
            apis: {
                /**
                * Goes back or forward the specified number of steps
                * A zero delta will reload the current page.
                * If the delta is out of range, does nothing.
                * @param int delta
                */
                go: function(delta){
                    remote.historyGo(delta, history);
                },
                /**
                * Goes back one step in the joint session history.
                */
                back: function(){
                    return this.go(-1);
                },
                /**
                * Goes back one step in the joint session history.
                */
                forward: function(){
                    return this.go(1);
                },
                /**
                * Pushes the given data onto the session history.
                * @param String data
                * @param String title
                * @param String url optional
                */
                pushState: function(data, title, url){
                    remote.historyPushState(data, title, url);
                },
                /**
                * Updates the current entry in the session history.
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
                popstate: function(callback){
                    popState.push(callback);
                }
            },
            internals: {
                historyMessage: function(name){
                    console.log("HISTORY MESSAGE");
                    for(var i in popState){
                        popstate[i]();
                    }
                }
            },
            stubs: ["historyPushState", "historyGo", "historyReplaceState"]
        };
    });

});
