//all requirejs mocks must be in real files to work with IE8
define('_xdmMockJiraTest', [], function () {
    return function() {
        if(typeof xdmMockJira !== 'undefined'){
            return xdmMockJira;
        }
    };
});