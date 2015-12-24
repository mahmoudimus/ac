define("ac/confluence/macro", ["confluence-editor/editor/atlassian-editor", "confluence/root", "confluence-editor/utils/tinymce-macro-utils", "confluence-macro-browser/macro-browser"],
        function(ConfluenceEditor, Confluence, MacroUtils, MacroBrowser) {

    var lastSelectedConnectMacroNode = undefined;
    var locationToInsertMacro = undefined;
    var unsavedMacroParams = undefined;
    var unsavedMacroBody = undefined;
    var unsavedMacroName = undefined;

    return {
        setLastSelectedConnectMacroNode: function(node) {
            lastSelectedConnectMacroNode = node;
        },

        getLastSelectedConnectMacroNode: function() {
            return lastSelectedConnectMacroNode;
        },

        setUnsavedMacroData: function(macroName, macroBody, macroParams, macroInsertLocation) {
            unsavedMacroBody = macroBody;
            unsavedMacroParams = macroParams;
            locationToInsertMacro = macroInsertLocation;
            unsavedMacroName = macroName;
        },

        /**
         * Returns the macro parameters of the last macro that was selected and set in this class.
         */
        getCurrentMacroParameters: function() {
            if (lastSelectedConnectMacroNode === undefined) {
                return unsavedMacroParams;
            }

            return MacroBrowser.getMacroParams(lastSelectedConnectMacroNode);
        },

        saveMacro: MacroUtils.updateMacro,

        /**
         * Saves the last selected macro with the provided parameters and body.
         *
         * @param {Object} updatedMacroParameters the updated parameters for the macro being edited.
         * @param {String} updatedMacroBody the (optional) body of the macro
         */
        saveCurrentMacro: function (updatedMacroParameters, updatedMacroBody) {
            if (lastSelectedConnectMacroNode === undefined) {
                //Must be saving macro that isn't yet on page. Store unsaved macro params
                unsavedMacroParams = updatedMacroParameters;
                unsavedMacroBody = updatedMacroBody;

                var macroRenderRequest = {
                    contentId: Confluence.getContentId(),
                    macro: {
                        name: unsavedMacroName,
                        params: unsavedMacroParams,
                        body: unsavedMacroBody
                    }
                };
                AJS.Rte.getEditor().selection.moveToBookmark(locationToInsertMacro);
                var insertedMacro = MacroUtils.insertMacro(macroRenderRequest);

                //Reset unsaved macro data.
                locationToInsertMacro = undefined;
                unsavedMacroParams = undefined;
                unsavedMacroBody = undefined;
                unsavedMacroName = undefined;

                return insertedMacro;
            }

            var macroName = lastSelectedConnectMacroNode.getAttribute('data-macro-name');

            if (macroName === null) {
                return undefined;
            }

            return MacroUtils.updateMacro(updatedMacroParameters, updatedMacroBody, macroName, lastSelectedConnectMacroNode);
        }
    };
});

