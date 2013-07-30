AP.define("confluence", ["_dollar", "_rpc"], function ($, rpc) {

    "use strict";

    return rpc.extend(function (remote) {
        return {
            apis: {
                saveMacro: function (macroParameters) {
                    remote.saveMacro(macroParameters);
                },
                closeMacroEditor: function () {
                    remote.closeMacroEditor();
                }
            }
        };
    });

});
