AP.define("confluence", ["_dollar", "_rpc"], 
    function ($, rpc) {
    "use strict";

    return rpc.extend(function (remote) {
        return {
            /**
            * Interact with the confluence macro editor.
            * @exports confluence
            */
            apis: {
                /**
                * Save a macro with data that can be accessed when viewing the confluence page.
                * @param {Object} data to be saved with the macro.
                * @example
                * AP.require('confluence', function(confluence){
                *   confluence.saveMacro({foo: 'bar'});
                * });
                */
                saveMacro: function (macroParameters) {
                    remote.saveMacro(macroParameters);
                },
                /**
                * Closes the macro editor, if it is open.
                * This call does not save any modified parameters to the macro, and saveMacro should be called first if necessary.
                * @example
                * AP.require('confluence', function(confluence){
                *   confluence.closeMacroEditor();
                * });
                */
                closeMacroEditor: function () {
                    remote.closeMacroEditor();
                }
            }
        };
    });

});
