//all requirejs mocks must be in real files to work with IE8
define('_xdmMockEnvTest', [], function () {
    return function() {
        if(typeof xdmMockEnv !== 'undefined'){
            return xdmMockEnv;
        }
    };
});