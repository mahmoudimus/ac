(function($, define){

    define("ac/confluence/macro/editor", ["connect-host", "ac/dialog", "ac/confluence/macro"], function(_AP, dialog, saveMacro) {

        var openEditorMacroBody;

        var module = {

            /**
             * Closes the macro editor if it is open. If you need to persist macro configuration, call <code>saveMacro</code>
             * before closing the editor.
             */
            close: function() {
                dialog.close();
            },

            /**
             * Returns the macro parameters of the macro being edited in the macro editor
             * @param callback the callback function which will be called with the parameter object
             */
            getMacroData: function(callback){
                return callback(saveMacro.getCurrentMacroParameters());
            },

            /**
             * Returns the macro body of the macro being edited in the macro editor
             * @param callback the callback function which will be called with the macro body
             */
            getMacroBody: function(callback){
                return callback(openEditorMacroBody);
            },

            /**
             * Constructs a new AUI dialog containing a custom editor proxied from a remote app. Should be passed to the
             * MacroBrowser as a macro editor override. (See editor-override.js for more details)
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
                // CE-74: if the editor loses focus before getBookmark() is called, a HierarchyRequestError
                // will occur in Internet Explorer, so restore focus just in case.
                AJS.Rte.getEditor().focus();
                var editorSelection = AJS.Rte.getEditor().selection;
                saveMacro.setLastSelectedConnectMacroNode(editorSelection.getNode());

                openEditorMacroBody = macroData.body;

                function getIframeHtmlForMacro(url) {
                    var data = {
                            "width": "100%",
                            "height": "100%",
                            "ui-params": _AP.uiParams.encode({dlg: 1})
                        };
                    $.extend(data, saveMacro.getCurrentMacroParameters());
                    return $.ajax(url, {
                        data: data
                    });
                }

                var dialogOpts = {
                    header: macroData.params ? opts.editTitle : opts.insertTitle,
                    submitText: macroData.params ? "Save" : "Insert",
                    chrome: true,
                    ns: macroData.name,
                    width: opts.width || null,
                    height: opts.height || null
                };

                dialog.create(dialogOpts, false);

                getIframeHtmlForMacro(opts.url).done(function(data){
                    var dialogHtml = $(data);
                    dialogHtml.addClass('ap-dialog-container');
                    $('.ap-dialog-container').replaceWith(dialogHtml);
                });

            }
        };

        return module;

    });


})(AJS.$, define);
