/**
 * In order for a Confluence macro to have a custom editing experience (ie. overriding the macro browser), the override
 * behaviour needs to be registered with the Macro Browser's JavaScript model at runtime. This file is a template that
 * gets injected into the dynamically-generated Remotable Plugin plugin for every Remote Macro module. The specifics of each
 * individual macro are injected into the variable names via a Web Resource Transformer module
 * (see MacroEditorInjectTransformer).
 */
_AP.require("dialog/simple", function(simpleDialog) {
  AJS.bind("init.rte", function () {

    // These parameters are injected contextually by the MacroEditorInjectorTransformer
    var macroName = "%%MACRONAME%%";
    var width = "%%WIDTH%%";
    var height = "%%HEIGHT%%";
    var editTitle = "%%EDIT_TITLE%%";
    var insertTitle = "%%INSERT_TITLE%%";
    var url = AJS.params.contextPath + "%%URL%%";
    var enc = encodeURIComponent;

    var customMacroOpener = {
      opener: function(data) {
        AJS.Rte.BookmarkManager.storeBookmark();

        var dialogOptions = {
          header: data.params ? editTitle : insertTitle,
          submitText: "Insert",
          submitHandler: function (dialog, result) {
            // Render the macro
            var macroRenderRequest = {
              contentId: Confluence.Editor.getContentId(),
              macro: {
                name: macroName,
                params: result.macroParameters,
                body: data.body ? data.body : "<p>&nbsp;</p>"
              }
            };
            tinymce.confluence.MacroUtils.insertMacro(macroRenderRequest);
          }
        };
        if (width) {
          dialogOptions.width = width;
        }
        if (height) {
          dialogOptions.height = height;
        }

        var additionalParams = AJS.$.extend({}, data.params, { body: data.body });
        var first = true;
        AJS.$.each(additionalParams, function(key, value) {
          url += first && url.indexOf("?") < 0 ? "?" : "&";
          url += enc(key) + "=" + enc(value);
          first = false;
        });

        var dialog = simpleDialog(url, dialogOptions);
        dialog.show();
      }
    };
    AJS.MacroBrowser.setMacroJsOverride(macroName, customMacroOpener);
  });
});
