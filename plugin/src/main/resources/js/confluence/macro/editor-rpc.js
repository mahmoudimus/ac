(function($, extend, require){
    "use strict";

    require(["ac/confluence/macro/editor"], function(editor) {
        extend(function () {
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

}(AJS.$, _AP.extend, require));