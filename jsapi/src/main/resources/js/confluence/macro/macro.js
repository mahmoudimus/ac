/**
 * @tainted tinymce.confluence.MacroUtils
 * @tainted AJS
 */
define("ac/confluence/macro", ["confluence/root", "confluence-macro-browser/macro-browser", "ajs"],
        function(Confluence, MacroBrowser, AJS) {

    var lastSelectedConnectMacroNode = undefined;
    var unsavedMacroData = {};

    return {
        setLastSelectedConnectMacroNode: function(node) {
            //Reset unsaved macro data.
            unsavedMacroData.locationToInsert = undefined;
            unsavedMacroData.params = undefined;
            unsavedMacroData.body = undefined;
            unsavedMacroData.name = undefined;

            lastSelectedConnectMacroNode = node;
        },

        getLastSelectedConnectMacroNode: function() {
            return lastSelectedConnectMacroNode;
        },

        setUnsavedMacroData: function(macroName, macroBody, macroParams, macroInsertLocation) {
            unsavedMacroData.body = macroBody;
            unsavedMacroData.params = macroParams;
            unsavedMacroData.locationToInsert = macroInsertLocation;
            unsavedMacroData.name = macroName;
        },

        /**
         * Returns the macro parameters of the last macro that was selected and set in this class.
         */
        getCurrentMacroParameters: function() {
            if (lastSelectedConnectMacroNode === undefined) {
                return unsavedMacroData.params;
            }

            return MacroBrowser.getMacroParams(lastSelectedConnectMacroNode);
        },

        //Wrapped to avoid tinymce not defined race condition.
        saveMacro: function() {
            return tinymce.confluence.MacroUtils.updateMacro.apply(this, arguments);
        },

        /**
         * Saves the last selected macro with the provided parameters and body.
         *
         * @param {Object} updatedMacroParameters the updated parameters for the macro being edited.
         * @param {String} updatedMacroBody the (optional) body of the macro
         */
        saveCurrentMacro: function (updatedMacroParameters, updatedMacroBody) {
            if (lastSelectedConnectMacroNode === undefined) {
                //Must be saving macro that isn't yet on page. Store unsaved macro params
                unsavedMacroData.params = updatedMacroParameters;
                unsavedMacroData.body = updatedMacroBody;

                var macroRenderRequest = {
                    contentId: Confluence.getContentId(),
                    macro: {
                        name: unsavedMacroData.name,
                        params: unsavedMacroData.params,
                        body: unsavedMacroData.body === null ? "" : unsavedMacroData.body
                    }
                };

                AJS.Rte.getEditor().selection.moveToBookmark(unsavedMacroData.locationToInsert);
                return tinymce.confluence.MacroUtils.insertMacro(macroRenderRequest);
            }

            var macroName = MacroBrowser.getMacroName(lastSelectedConnectMacroNode);

            if (macroName === undefined) {
                return undefined;
            }

            return tinymce.confluence.MacroUtils.updateMacro(updatedMacroParameters, updatedMacroBody, macroName, lastSelectedConnectMacroNode);
        }
    };
});

