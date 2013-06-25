_AP.define("confluence/macro/editor", ["_dollar", "dialog/simple"], function($, simpleDialog) {

    var enc = encodeURIComponent;
    var saveMacro;

    return {

        saveMacro: function(updatedParameters) {
            if (!saveMacro) {
                $.handleError("Illegal state: no macro currently being edited!");
            }
            saveMacro(updatedParameters);
            saveMacro = null;
        },

        openCustomEditor: function(data, injectedParams) {
            AJS.Rte.BookmarkManager.storeBookmark();

            saveMacro = function(updatedParameters) {
                // Render the macro
                var macroRenderRequest = {
                    contentId: Confluence.Editor.getContentId(),
                    macro: {
                        name: injectedParams.macroName,
                        params: updatedParameters,
                        body: data.body ? data.body : "<p>&nbsp;</p>"
                    }
                };
                tinymce.confluence.MacroUtils.insertMacro(macroRenderRequest);
            };

            var dialogOptions = {
                header: data.params ? injectedParams.editTitle : injectedParams.insertTitle,
                submitText: "Insert"
            };
            if (injectedParams.width) {
                dialogOptions.width = injectedParams.width;
            }
            if (injectedParams.height) {
                dialogOptions.height = injectedParams.height;
            }

            var additionalParams = AJS.$.extend({}, data.params, { body: data.body });
            var first = true;
            AJS.$.each(additionalParams, function(key, value) {
                injectedParams.url += first && injectedParams.url.indexOf("?") < 0 ? "?" : "&";
                injectedParams.url += enc(key) + "=" + enc(value);
                first = false;
            });

            var dialog = simpleDialog(injectedParams.url, dialogOptions);
            dialog.show();
        }

    };

});