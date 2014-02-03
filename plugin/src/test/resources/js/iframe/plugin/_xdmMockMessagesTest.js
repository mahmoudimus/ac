//all requirejs mocks must be in real files to work with IE8
define('_xdmMockMessagesTest', [], function () {
    return function() {
        if(typeof xdmMockMessages !== 'undefined'){
            return xdmMockMessages;
        }
    };
});
