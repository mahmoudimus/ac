//all requirejs mocks must be in real files to work with IE8
define('_xdmMockInlineDialogTest', [], function () {
    return function() {
        if(typeof xdmMockInlineDialog !== 'undefined'){
            return xdmMockInlineDialog;
        }
    };
});