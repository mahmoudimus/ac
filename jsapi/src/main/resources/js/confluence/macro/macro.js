/**
 * @tainted tinymce.confluence.MacroUtils
 * @tainted AJS
 */
define("ac/confluence/macro", ["confluence/root", "confluence-macro-browser/macro-browser", "ajs"],
        function(Confluence, MacroBrowser, AJS) {

    var lastSelectedConnectMacroNode = undefined;
    var newMacroData = {};

    return {
        setLastSelectedConnectMacroNode: function(node) {
            //Reset unsaved macro data.
            newMacroData.locationToInsert = undefined;
            newMacroData.params = undefined;
            newMacroData.body = undefined;
            newMacroData.name = undefined;

            var macroName = MacroBrowser.getMacroName(node);
            if(macroName !== undefined) {
                lastSelectedConnectMacroNode = node;
            } else {
                lastSelectedConnectMacroNode = undefined;
            }
        },

        setUnsavedMacroData: function(macroName, macroBody, macroParams, macroInsertLocation) {
            newMacroData.body = macroBody;
            newMacroData.params = macroParams;
            newMacroData.locationToInsert = macroInsertLocation;
            newMacroData.name = macroName;
        },

        /**
         * Returns the macro parameters of the last macro that was selected and set in this class.
         */
        getCurrentMacroParameters: function() {
            if (lastSelectedConnectMacroNode === undefined) {
                return newMacroData.params;
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
                newMacroData.params = updatedMacroParameters;
                newMacroData.body = updatedMacroBody;

                var macroRenderRequest = {
                    contentId: Confluence.getContentId(),
                    macro: {
                        name: newMacroData.name,
                        params: newMacroData.params,
                        body: newMacroData.body === null ? "" : newMacroData.body
                    }
                };

                AJS.Rte.getEditor().selection.moveToBookmark(newMacroData.locationToInsert);
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

