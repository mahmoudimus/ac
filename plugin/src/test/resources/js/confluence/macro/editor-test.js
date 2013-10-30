require.config({
  map: {
    '*': {
      'dialog/simple': 'mock/dialog/simple',
    }
  }
});

var mockSimpleDialog;
var mockConfluence = {};


define(['confluence/macro/editor'], function() {

  _AP.require(["confluence/macro/editor"], function(confluenceMacroEditor) {

    module("Confluence Macro Editor", {
      setup: function() {
        dialogSpy = sinon.stub();
        dialogSpy.returns({
            show: sinon.spy(),
            on: sinon.spy(),
            remove: sinon.spy(),
            hide: sinon.spy()
        });

        MacroEditorOpts = {
            insertTitle: 'insert foo bar'
        };
        MacroData = {
            name: 'foo bar'
        };
        AJS.Rte = {
          BookmarkManager: {
            storeBookmark: sinon.spy()
          }
        };
        this.server = sinon.fakeServer.create();
        
        _AP.define('dialog/simple', function () {
            return dialogSpy;
        });
    
      },
      teardown: function() {
        this.server.restore();
        // remove any dialog elements
        // clean up mock
        AJS.Rte = null;
      }
    });

    test("Dialog is shown when openCustomEditor is called", function () {
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        ok(dialogSpy().show.calledOnce, 'Dialog show was called');
        
    });

    test("Dialog width is set", function () {
        MacroEditorOpts.width = 123;
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogSpy.args[0][1].width, MacroEditorOpts.width, 'dialog width is set to 123');
    });

    test("Dialog height is set", function () {
        MacroEditorOpts.height = 321;
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogSpy.args[0][1].height, MacroEditorOpts.height, 'dialog height is set to 321');
    });

    test("Dialog header is set", function () {
        MacroEditorOpts.insertTitle = 'insert title';
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogSpy.args[0][1].header, MacroEditorOpts.insertTitle, 'dialog header is set to insert title');
    });

    test("dialog header uses edit title when editing", function () {
        MacroEditorOpts.insertTitle = 'insert title';
        MacroEditorOpts.editTitle = 'edit title';
        MacroEditorOpts.url = '/foo/?foo';
        MacroData.params = {
            foo: 'bar'
        };
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogSpy.args[0][1].header, MacroEditorOpts.editTitle, 'dialog header is set to edit title');
    });

    test("dialog header uses insert title when inserting", function () {
        MacroEditorOpts.insertTitle = 'insert title';
        MacroEditorOpts.editTitle = 'edit title';
        MacroEditorOpts.url = '/foo/?foo';
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogSpy.args[0][1].header, MacroEditorOpts.insertTitle, 'dialog header is set to insert title');
    });

    test("dialog url contains parameters", function () {
        MacroEditorOpts.url = '/abc/';
        MacroData.params = {
            foo: 'bar'
        };
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        ok(dialogSpy.args[0][0].match(/foo\=bar/i).length > 0, 'dialog url contains additional parameters');
    });

    test("saveMacro writes macro to page", function () {
        tinymce = {
            confluence: {
                MacroUtils: {
                    insertMacro: sinon.spy()
                }
            }
        };

        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        confluenceMacroEditor.saveMacro();
        ok(tinymce.confluence.MacroUtils.insertMacro.calledOnce, 'saveMacro calls the confluence macro save function');
    });

  });
});
