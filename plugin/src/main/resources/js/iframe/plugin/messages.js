AP.define("messages", ["_dollar", "_rpc"],

/**
* @exports messages
*/

function ($, rpc) {
    "use strict";

    var exports;

    rpc.extend(function (remote) {
        exports = {

            show: function () {
                remote.hideInlineDialog();
            },

            clear: function () {
                
            }
        }
        return {
            stubs: ['hideInlineDialog']
        }
    });

    return exports;

});
