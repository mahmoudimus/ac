//all requirejs mocks must be in real files to work with IE8
define('_xdmMockCookieTest', [], function () {
    return function() {
        if(typeof xdmMockCookie !== 'undefined'){
            return xdmMockCookie;
        }
    };
});