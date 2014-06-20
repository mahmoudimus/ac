_AP.define("confluence/macro/editor/rpc", ["_dollar", "confluence/macro/editor", "_rpc"], function($, editor, rpc) {
    rpc.extend(function () {
        return {
            internals: {
                saveMacro: function (updatedParams) {
                    editor.saveMacro(updatedParams);
                },
                closeMacroEditor: function () {
                    editor.close();
                },
                getMacroData: function (callback) {
                    editor.getMacroData(callback);
                }
            }
        };
    });

});
