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
        init: function() {},
        hideInlineDialog: sinon.spy()
    };

    context(["_rpc", "inline-dialog"], function(_rpc, inlineDialog) {
        _rpc.init();

        module("inlineDialog plugin", {
            setup: function(){
                xdmMockInlineDialog.hideInlineDialog.reset();
            }
        });

        test('hideInlineDialog calls remote hideInlineDialog', function(){
            inlineDialog.hideInlineDialog();
            ok(xdmMockInlineDialog.hideInlineDialog.calledOnce);
        });

        test('onHide callback is executed when hidden', function(){
            var spy = sinon.spy();
            inlineDialog.onHide(spy);
            inlineDialog.hideInlineDialog();
            ok(spy.calledOnce);
        });

        test('returning false in onHide callback stops the dialog closing', function(){
            var mock = sinon.stub().returns(false);
            inlineDialog.onHide(mock);

            inlineDialog.hideInlineDialog();
            ok(xdmMockInlineDialog.hideInlineDialog.called === false);
        });


    });

})();
