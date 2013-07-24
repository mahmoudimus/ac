AP.define("confluence", ["_dollar", "_rpc"], function ($, rpc) {

    "use strict";

    return rpc.extend(function (remote) {
        return {
            apis: {
                saveMacro: function (macroParameters, dialogOptions) {
                    remote.saveMacro(macroParameters, dialogOptions);
                }
            }
        };
    });

});
