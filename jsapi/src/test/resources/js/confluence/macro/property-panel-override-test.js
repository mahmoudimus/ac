define(['Squire'], function(Squire) {
    var injector = new Squire();
    var mockAjs =  {
        Rte : {
            getEditor: function() {
                return {
                    selection: {
                        getNode: function() {
                            return {};
                        }
                    }
                };
            }
        }
    };

    injector
            .mock('ajs', mockAjs)
            .mock('confluence/root', {})
            .mock('confluence-editor/utils/tinymce-macro-utils', {})
            .mock('confluence-macro-browser/macro-browser', {})
            .mock('confluence-editor/editor/atlassian-editor', {})
            .require(['ac/confluence/macro/property-panel-iframe'], function (propertyPanelIframeInjector) {

                const MACRO_URL = "MACRO_URL";
                const macroUIParams = "eyJkbGciOjF9";
                var currentPropertyPanel = {};

                module("Property Panel tests");

                test("Ensure property panel iframe returns function", function() {
                    equal(typeof propertyPanelIframeInjector, "function");
                });

                test("Ensure that property panel function returns object", function() {
                    equal(typeof propertyPanelIframeInjector(MACRO_URL), "object");
                });

                test("Ensure that property panel object contains expected property", function() {
                    equal(typeof propertyPanelIframeInjector(MACRO_URL).propertyPanelIFrameInjector, "function");
                });

                test("Ensure that property panel ajax call contains expected data", function() {
                    sinon.stub($, "ajax", function() {
                        return {done: function(){}}
                    });
                    propertyPanelIframeInjector(MACRO_URL).propertyPanelIFrameInjector(currentPropertyPanel);
                    equal($.ajax.calledOnce, true);
                    equal($.ajax.getCall(0).args[0], MACRO_URL);
                    equal($.ajax.getCall(0).args[1].data['classifier'], "property-panel");
                    equal($.ajax.getCall(0).args[1].data['ui-params'], macroUIParams);
                    equal(typeof propertyPanelIframeInjector(MACRO_URL), "object");
                    $.ajax.restore();
                });

                test("Ensure that new iframe is appended to property panel", function(){
                    currentPropertyPanel.panel = {append: function(){}};
                    var appendStub = sinon.stub(currentPropertyPanel.panel, "append");

                    var cssStub = sinon.stub();

                    var returnedFromJquery = {css : cssStub};

                    sinon.stub(AJS.$, "ajax").returns({done: function(f){f();}});
                    //$.fn.init is the actual method that is called when you call $()
                    sinon.stub(AJS.$.fn, "init").returns(returnedFromJquery);

                    propertyPanelIframeInjector(MACRO_URL).propertyPanelIFrameInjector(currentPropertyPanel);

                    //Ensure that the iframe is hidden
                    equal(cssStub.calledOnce, true);
                    equal(cssStub.getCall(0).args[0], 'display');
                    equal(cssStub.getCall(0).args[1], 'none');

                    //Ensure that it's appended to the current property panel
                    equal(appendStub.calledOnce, true);
                    equal(appendStub.getCall(0).args[0], returnedFromJquery);

                    AJS.$.ajax.restore();
                    AJS.$.fn.init.restore();
                });
            });
});