define("ac/confluence/macro", ["confluence-editor/editor/atlassian-editor", "confluence/root", "confluence-editor/utils/tinymce-macro-utils", "confluence-macro-browser/macro-browser"],
        function(ConfluenceEditor, Confluence, MacroUtils, MacroBrowser) {

    var lastSelectedConnectMacroNode = undefined;

    return {
        setLastSelectedConnectMacroNode: function(node) {
            lastSelectedConnectMacroNode = node;
        },

        getLastSelectedConnectMacroNode: function() {
            return lastSelectedConnectMacroNode;
        },

        /**
         * Returns the macro parameters of the last macro that was selected and set in this class.
         */
        getCurrentMacroParameters: function() {
            if (lastSelectedConnectMacroNode === undefined) {
                return undefined;
            }

            return MacroBrowser.getMacroParams(lastSelectedConnectMacroNode);
        },

        saveMacro: MacroUtils.updateMacro,

        /**
         * Saves the macro currently being edited. Relies on openCustomEditor() first being invoked by MacroBrowser.
         *
         * @param {Object} updatedMacroParameters the updated parameters for the macro being edited.
         * @param {String} updatedMacroBody the (optional) body of the macro
         */
        saveCurrentMacro: function (updatedMacroParameters, updatedMacroBody) {
            if (lastSelectedConnectMacroNode === undefined) {
                return undefined;
            }

            var macroName = lastSelectedConnectMacroNode.getAttribute('data-macro-name');

            if (macroName === null) {
                return undefined;
            }

            return MacroUtils.updateMacro(updatedMacroParameters, updatedMacroBody, macroName, lastSelectedConnectMacroNode);
        }
    };
});

