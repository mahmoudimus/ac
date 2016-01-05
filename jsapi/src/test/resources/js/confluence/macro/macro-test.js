define(['Squire'], function(Squire) {
    var injector = new Squire();

    var oldTinymce = undefined;

    const MACRO_NAME_FROM_DOM = "NameFromDom";
    const CONTENT_ID = "My ID";


    var mockAjs =  {
        Rte : {
            getEditor: function() {
                return {
                    selection: {
                        moveToBookmark: function() {

                        }
                    }
                };
            }
        }
    };

    injector
            .mock('ajs', mockAjs)
            .mock('confluence/root', {
                getContentId: function() {
                    return CONTENT_ID;
                }
            })
            .mock('confluence-macro-browser/macro-browser',
            {
                getMacroName: function() {
                    return MACRO_NAME_FROM_DOM;
                }
            })
            .require(['ac/confluence/macro'], function (Macro) {

                module("Macro API tests", {
                    beforeEach: function() {
                        oldTinymce = window.tinymce;
                        window.tinymce = {
                            confluence: {
                                MacroUtils: {
                                    insertMacro: sinon.stub(),
                                    updateMacro: sinon.stub()
                                }
                            }
                        };
                    },
                    teardown: function() {
                        window.tinymce = oldTinymce;
                        Macro.setLastSelectedConnectMacroNode(undefined);
                    }
                });

                test("Save and get last selected macro node", function() {
                    var nodeName = "This is my node.";
                    var node = {name: nodeName};
                    Macro.setLastSelectedConnectMacroNode(node);
                    ok(Macro.getLastSelectedConnectMacroNode().name === nodeName);
                });

                test("Unsaved macro data", function() {
                    //Last selected macro node will be undefined.
                    var macroParams = {paramOne: "One"};
                    var macroName = "MyName";
                    Macro.setUnsavedMacroData(macroName, undefined, macroParams, undefined);
                    ok(Macro.getCurrentMacroParameters().paramOne === macroParams.paramOne);
                });

                //Unsaved data should be overridden (in case where macro already exists on page)
                test("Save current macro doesn't keep unsaved data", function() {
                    var selectedNode = {};
                    Macro.setLastSelectedConnectMacroNode(selectedNode);
                    Macro.setUnsavedMacroData("MyName", undefined, {paramOne: "This thing"}, undefined);
                    var realBody = "My macro body";
                    var realParams = {paramOne: "My real parameters"};
                    Macro.saveCurrentMacro(realParams, realBody);
                    var updateCall = window.tinymce.confluence.MacroUtils.updateMacro.getCall(0);

                    ok(updateCall.args.length === 4);
                    ok(updateCall.args[0].paramOne === realParams.paramOne);
                    ok(updateCall.args[1] === realBody);
                    ok(updateCall.args[2] === MACRO_NAME_FROM_DOM);
                    ok(updateCall.args[3] === selectedNode);
                });

                test("Save unsaved macro functionality", function() {
                    var macroName = "MyName";
                    Macro.setUnsavedMacroData(macroName, undefined, {paramOne: "This thing"}, undefined);
                    var realBody = "My macro body";
                    var realParams = {paramOne: "My real parameters"};
                    Macro.saveCurrentMacro(realParams, realBody);
                    var updateCall = window.tinymce.confluence.MacroUtils.insertMacro.getCall(0);

                    ok(updateCall.args.length === 1);
                    ok(updateCall.args[0].contentId === CONTENT_ID);
                    ok(updateCall.args[0].macro.name === macroName);
                    ok(updateCall.args[0].macro.params.paramOne === realParams.paramOne);
                    ok(updateCall.args[0].macro.body === realBody);

                    //Check that it stored the newly saved parameters.
                    ok(Macro.getCurrentMacroParameters().paramOne === realParams.paramOne);
                });
            });
});