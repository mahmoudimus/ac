_AP.define("confluence/macro/editor", ["_dollar", "dialog/simple"], function($, simpleDialog) {

    var enc = encodeURIComponent;

    // When openCustomEditor is invoked, it will assign a function for saving the macro
    // being edited to this field. This simplifies the client's job of saving the macro
    // values - they only need to pass back the updated values - and works because only
    // a single macro editor can be open at a time.
    var saveMacro;

    return {

        /**
         * Saves the macro currently being edited. Relies on openCustomEditor() first being invoked by MacroBrowser.
         *
         * @param {Object} updatedParameters the updated parameters for the macro being edited.
         */
        saveMacro: function(updatedParameters) {
            if (!saveMacro) {
                $.handleError("Illegal state: no macro currently being edited!");
            }
            saveMacro(updatedParameters);
            saveMacro = undefined;
        },

        /**
         * Constructs a new AUI dialog containing a custom editor proxied from a remote app. Should be passed to the
         * MacroBrowser as a macro editor override. (See override.js for more details)
         *
         * @param {Object} macroData Data associated with the macro being edited.
         * @param {String} [macroData.name] the macro's name.
         * @param {String} [macroData.body] the body content of the macro (if any).
         * @param {Object} [macroData.params] stored key-value parameters associated with the macro.
         * @param {Object} opts Options to configure the behaviour and appearance of the editor dialog.
         * @param {String} [opts.url] url targeting a local proxy servlet for the remote application's custom macro editor.
         * @param {String} [opts.editTitle="Remotable Plugins Dialog Title"] dialog header to be used when editing an existing macro.
         * @param {String} [opts.insertTitle="Remotable Plugins Dialog Title"] dialog header to be used when inserting the macro for the first time.
         * @param {String|Number} [opts.width="50%"] width of the dialog, expressed as either absolute pixels (eg 800) or percent (eg 50%)
         * @param {String|Number} [opts.height="50%"] height of the dialog, expressed as either absolute pixels (eg 600) or percent (eg 50%)
         */
        openCustomEditor: function(macroData, opts) {
            AJS.Rte.BookmarkManager.storeBookmark();

            saveMacro = function(updatedParameters) {
                // Render the macro
                var macroRenderRequest = {
                    contentId: Confluence.Editor.getContentId(),
                    macro: {
                        name: macroData.name,
                        params: updatedParameters,
                        body: macroData.body ? macroData.body : "<p>&nbsp;</p>"
                    }
                };
                tinymce.confluence.MacroUtils.insertMacro(macroRenderRequest);
            };

            var dialogOpts = {
                header: macroData.params ? opts.editTitle : opts.insertTitle,
                submitText: "Insert"
            };

            if (opts.width) {
                dialogOpts.width = opts.width;
            }
            if (opts.height) {
                dialogOpts.height = opts.height;
            }

            var url = opts.url;
            var additionalParams = AJS.$.extend({}, macroData.params, { body: macroData.body });
            var first = true;
            AJS.$.each(additionalParams, function(key, value) {
                url += first && url.indexOf("?") < 0 ? "?" : "&";
                url += enc(key) + "=" + enc(value);
                first = false;
            });

            var dialog = simpleDialog(url, dialogOpts);
            dialog.show();
        }

    };

});