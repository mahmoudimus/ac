//all requirejs mocks must be in real files to work with IE8
define('_xdmMockRequestTest', [], function () {
    return function() {
        if(typeof xdmMockRequest !== 'undefined'){
            return xdmMockRequest;
        }
    };
});
