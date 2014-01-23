var xdmMockInlineDialog;
(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin',
        map: {
            '*': {
                '_xdm': '_xdmMockInlineDialogTest'
            }
        },
        paths: {
            '_xdmMockInlineDialogTest': '/base/src/test/resources/js/iframe/plugin/_xdmMockInlineDialogTest'
        }
    });

    xdmMockInlineDialog = {
        hideInlineDialog: sinon.spy(),
        init: function() {}
    };

    context(["_rpc", "inline-dialog"], function(_rpc, inlineDialog) {
        _rpc.init();

        module("Inline Dialog plugin", {
            setup: function(){
                xdmMockInlineDialog.hideInlineDialog.reset();
            }
        });

        test('hide calls remote hideInlineDialog', function(){
            inlineDialog.hide();
            ok(xdmMockInlineDialog.hideInlineDialog.calledOnce);
        });

    });

})();
