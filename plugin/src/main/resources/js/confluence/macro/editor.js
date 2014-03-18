_AP.define("confluence/macro/editor", ["_dollar", "dialog/dialog-factory"], function($, dialogFactory) {

    var enc = encodeURIComponent;

    // When openCustomEditor is invoked, it will assign a function for saving the macro
    // being edited to this field. This simplifies the client's job of saving the macro
    // values - they only need to pass back the updated values - and works because only
    // a single macro editor can be open at a time.
    var saveMacro,
        macroEditorDialog,
        openEditorMacroData;

    return {
        getMacroData: function(callback){
            return callback(openEditorMacroData);
        },
        /**
         * Closes the macro editor if it is open. If you need to persist macro configuration, call <code>saveMacro</code>
         * before closing the editor.
         */
        close: function() {
            if (macroEditorDialog && macroEditorDialog.close) {
                macroEditorDialog.close();
            }
            macroEditorDialog = undefined;
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

            openEditorMacroData = macroData.params;
            saveMacro = function(updatedParameters) {
                // Render the macro
                var macroRenderRequest = {
                    contentId: Confluence.Editor.getContentId(),
                    macro: {
                        name: macroData.name,
                        params: updatedParameters,
                        // AC-741: MacroUtils clients in Confluence core set a non-existent macro body to the empty string.
                        // In the absence of a public API, let's do the same to minimize the chance of breakage in the future.
                        body: macroData.body ? macroData.body : ""
                    }
                };
                tinymce.confluence.MacroUtils.insertMacro(macroRenderRequest);
            };

            var dialogOpts = {
                header: macroData.params ? opts.editTitle : opts.insertTitle,
                submitText: macroData.params ? "Save" : "Insert"
            };

            if (opts.width) {
                dialogOpts.width = opts.width;
            }
            if (opts.height) {
                dialogOpts.height = opts.height;
            }

            var url = opts.url;

            var macroUrl = url.match(/\/servlet\/(ac|atlassian\-connect)\/([\w-]+)\/([\w-]+)/);
            var dialogPageOptions = {
                key: macroUrl[2],
                moduleKey: macroUrl[3],
                chrome: true,
                uiParams: $.extend({}, macroData.params, { body: macroData.body })
            };

            dialogFactory(dialogPageOptions, dialogOpts);
        },

        /**
         * Saves the macro currently being edited. Relies on openCustomEditor() first being invoked by MacroBrowser.
         *
         * @param {Object} updatedMacroParameters the updated parameters for the macro being edited.
         */
        saveMacro: function(updatedMacroParameters) {
            if (!saveMacro) {
                $.handleError("Illegal state: no macro currently being edited!");
            }
            saveMacro(updatedMacroParameters);
            saveMacro = undefined;
        }


    };

});
