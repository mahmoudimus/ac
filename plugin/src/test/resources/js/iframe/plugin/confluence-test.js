(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin',
        map: {
            '*': {
                '_xdm': '_xdmMockConfluenceTest'
            }
        },
        paths: {
            '_xdmMockConfluenceTest': '/base/src/test/resources/js/iframe/plugin/_xdmMockConfluenceTest'
        }
    });

    window.xdmMock = {
        init: function() {},
        saveMacro: sinon.spy(),
        getMacroData: sinon.spy(),
        getMacroBody: sinon.spy(),
        closeMacroEditor: sinon.spy()
    };

    context(["confluence"], function() {
        AP.require(['confluence'],function(confluence){
            xdmMock.saveMacro = sinon.spy();
            xdmMock.closeMacroEditor = sinon.spy();
            xdmMock.getMacroData = sinon.spy();

            module("Confluence Plugin", {
                setup: function() {
                    xdmMock.saveMacro.reset();
                    xdmMock.closeMacroEditor.reset();
                    xdmMock.getMacroData.reset();
                }
            });

            test("saveMacro", function() {
                confluence.saveMacro("1");
                ok(xdmMock.saveMacro.calledOnce);
            });

            test("closeMacro", function(){
                confluence.closeMacroEditor();
                ok(xdmMock.closeMacroEditor.calledOnce);
            });

            test("getMacroData", function() {
                var callback = sinon.spy();
                confluence.getMacroData(callback);
                ok(xdmMock.getMacroData.calledOnce);
            });

        });

    });

})();
