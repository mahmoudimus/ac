var mockSimpleDialog;
var mockConfluence = {};

define(['ac/confluence/macro/editor', 'connect-host'], function(confluenceMacroEditor, _AP) {

    var dialogSpy;

    module("Confluence Macro Editor", {
      setup: function() {
        MacroEditorOpts = {
            insertTitle: 'insert foo bar'
        };
        MacroData = {
            name: 'foo bar'
        };
        AJS.Rte = {
          getEditor: function() {
            return {
              focus: sinon.spy(),
              selection: {
                getBookmark: sinon.spy(),
                moveToBookmark: sinon.spy()
              }
            };
          }
        };
        this.server = sinon.fakeServer.create();

        AJS.dialog2 = function($el) {
          dialogSpy = {
            show: sinon.spy(),
            on: sinon.spy(),
            remove: sinon.spy(),
            hide: sinon.spy(),
            $el: $el
          };
          return dialogSpy;
        };
        //mock main Confluence object
        window.Confluence = {
            Editor: {
                getContentId: sinon.stub().returns('12345')
            }
        };
        //mock tinymce
        tinymce = {
            confluence: {
                MacroUtils: {
                    insertMacro: sinon.spy()
                }
            }
        };
        this.layerSpy = {
            changeSize: sinon.spy()
        };
        AJS.layer = sinon.stub().returns(this.layerSpy);

      },
      teardown: function() {
        this.server.restore();
        // remove any dialog elements
        // clean up mocks
        AJS.Rte = null;
        window.Confluence = null;
        tinymce = null;
        MacroData = null;
        MacroEditorOpts = null;
        dialogSpy = null;
        AJS.layer = null;
        AJS.dialog2 = null;
      }
    });

    function dialogElement() {
        return dialogSpy.$el;
    }
    
    test("Dialog is shown when openCustomEditor is called", function () {
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        ok(dialogSpy.show.calledOnce, 'Dialog show was called');
    });

    test("Dialog width is set", function () {
        MacroEditorOpts.width = 123;
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(this.layerSpy.changeSize.args[0][0], MacroEditorOpts.width, 'dialog width is set to 123');
    });

    test("Dialog height is set", function () {
        MacroEditorOpts.height = 321;
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(this.layerSpy.changeSize.args[0][1], MacroEditorOpts.height, 'dialog width is set to 321');
    });


    test("Dialog header is set", function () {
        MacroEditorOpts.insertTitle = 'insert title';
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogElement().find('h1').text(), MacroEditorOpts.insertTitle, 'dialog header is set to "insert title"');
    });

    test("dialog header uses edit title when editing", function () {
        MacroEditorOpts.insertTitle = 'insert title';
        MacroEditorOpts.editTitle = 'edit title';
        MacroEditorOpts.url = '/servlet/atlassian-connect/modulekey/pluginkey/?foo';
        MacroData.params = {
            foo: 'bar'
        };
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogElement().find('h1').text(), MacroEditorOpts.editTitle, 'dialog header is set to "edit title"');
    });

    test("dialog header uses insert title when inserting", function () {
        MacroEditorOpts.insertTitle = 'insert title';
        MacroEditorOpts.editTitle = 'edit title';
        MacroEditorOpts.url = '/servlet/atlassian-connect/modulekey/pluginkey/?foo';
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        equal(dialogElement().find('h1').text(), MacroEditorOpts.insertTitle, 'dialog header is set to "insert title"');
    });

    test("dialog url contains parameters", function () {
        this.server = sinon.fakeServer.create();
        this.server.respondWith("GET", /.*modulekey\/pluginkey/,
            [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

        MacroEditorOpts.url = '/servlet/atlassian-connect/modulekey/pluginkey/';
        MacroData.params = {
            foo: 'bar'
        };
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        ok(this.server.requests[0].url.match(/foo\=bar/i).length > 0, 'dialog url contains additional parameters');
        this.server.restore();
    });

    test("saveMacro writes macro to page", function () {
        MacroEditorOpts.url = '/servlet/atlassian-connect/modulekey/pluginkey/';
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        confluenceMacroEditor.saveMacro();
        ok(tinymce.confluence.MacroUtils.insertMacro.calledOnce, 'saveMacro calls the confluence macro save function');
    });

    test("getMacroData returns the macro data", function () {
        MacroData.params = {
            foo: 'bar'
        };
        MacroEditorOpts.url = '/servlet/atlassian-connect/modulekey/pluginkey/';
        confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
        var spy = sinon.spy();
        confluenceMacroEditor.getMacroData(spy);
        equal(spy.args[0][0], MacroData.params, 'getMacroData passes the macro data to the callback function');
    });

    test("getMacroBody returns the macro body", function () {
      MacroData.body = "<p>macro body</p>";
      MacroEditorOpts.url = '/servlet/atlassian-connect/modulekey/pluginkey/';
      confluenceMacroEditor.openCustomEditor(MacroData, MacroEditorOpts);
      var spy = sinon.spy();
      confluenceMacroEditor.getMacroBody(spy);
      equal(spy.args[0][0], MacroData.body, 'getMacroBody passes the macro body to the callback function');
    });

});
