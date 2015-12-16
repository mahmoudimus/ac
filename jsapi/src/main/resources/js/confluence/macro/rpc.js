(function($, require){
    "use strict";

    require(['ac/confluence/macro/editor', 'ac/confluence/macro', 'connect-host'], function(editor, macro, _AP) {
        _AP.extend(function () {
            return {
                internals: {
                    saveMacro: macro.saveCurrentMacro,
                    closeMacroEditor: editor.close,
                    getMacroData: editor.getMacroData,
                    getMacroBody: editor.getMacroBody
                }
            };
        });
    });

}(AJS.$, require));