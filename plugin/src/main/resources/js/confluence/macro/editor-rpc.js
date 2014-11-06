require(["_dollar", "confluence/macro/editor", "_rpc"], function($, editor, rpc) {
    rpc.extend(function () {
        return {
            internals: {
                saveMacro: function (updatedParams, updatedMacroBody) {
                    editor.saveMacro(updatedParams, updatedMacroBody);
                },
                closeMacroEditor: function () {
                    editor.close();
                },
                getMacroData: function (callback) {
                    editor.getMacroData(callback);
                },
                getMacroBody: function (callback) {
                    editor.getMacroBody(callback);
                }
            }
        };
    });

});
