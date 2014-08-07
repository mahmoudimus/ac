var xdmMockRequest;
(function(){

    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin',

        map: {
            '*': {
                '_xdm': '_xdmMockRequestTest'
            }
        },
        paths: {
            '_xdmMockRequestTest': '/base/src/test/resources/js/iframe/plugin/_xdmMockRequestTest'
        }


    });

    xdmMockRequest = {
        init: function() {},
        request: sinon.spy()
    };

    context(["_rpc", "env", "_dollar"], function(_rpc, env, $) {
        _rpc.init();
        context(["request"], function(request){
            module("Request Plugin", {

                teardown: function () {
                    xdmMockRequest.request.reset();
                },

                isFunction: function(functionToCheck) {
                    return functionToCheck && (functionToCheck instanceof Function);
                },
                invokeSuccessRequestResponse: function(args){
                    return xdmMockRequest.request.args[0][1]( args );
                },
                invokeErrorRequestResponse: function(args){
                    return xdmMockRequest.request.args[0][2]( args );
                }
            });

            test("invokes to host request", function () {
                request("/foo/bar");
                ok(xdmMockRequest.request.calledOnce, "invokes host request function");
            });

            test("host request is passed url", function () {
                request("/foo/bar");

                equal(xdmMockRequest.request.args[0][0].url, "/foo/bar", "passes correct url");
            });

            test("host request is passed success callback", function () {
                request('/foo/bar');

                ok(this.isFunction(xdmMockRequest.request.args[0][1]));
            });

            test("host request is passed fail callback", function () {
                request('/foo/bar');

                ok(this.isFunction(xdmMockRequest.request.args[0][2]));
            });

            test("host request is passed cache param", function () {
                request('/foo/bar', {cache: true});
                equal(xdmMockRequest.request.args[0][0].cache, true);
            });

            test("custom success callback passed", function () {
                var successSpy = sinon.spy();

                request('/foo/bar', {success: successSpy});
                //execute success response from host
                this.invokeSuccessRequestResponse( [{}, {}, {}] );

                ok(successSpy.calledOnce, 'success callback is executed on success');
            });

            test("custom error callback passed", function () {
                var failureSpy = sinon.spy();

                request('/foo/bar', {error: failureSpy});
                //execute error response from host
                this.invokeErrorRequestResponse( [{}, {}, {}] );

                ok(failureSpy.calledOnce, 'error callback is executed on failure');
            });

            test("success callback getAllResponseHeaders returns a formatted string of headers", function () {
                var successSpy = sinon.spy(),
                successHeaders = {
                    abc: 123
                };

                request('/foo/bar', {success: successSpy});
                //execute success response from host
                this.invokeSuccessRequestResponse( [{}, {}, {headers: successHeaders}] );
                equal(successSpy.args[0][2].getAllResponseHeaders(), 'abc: 123', 'all success headers are returned');
            });

            test("success callback getResponseHeader returns the response header", function () {
                var successSpy = sinon.spy(),
                successHeaders = {
                    abc: 123
                };

                request('/foo/bar', {success: successSpy});
                //execute success response from host
                this.invokeSuccessRequestResponse( [{}, {}, {headers: successHeaders}] );

                equal(successSpy.args[0][2].getResponseHeader('abc'), 123, 'correct value is returned for header');

            });

            test("error callback getAllResponseHeaders returns a formatted string of headers", function () {
                var errorSpy = sinon.spy(),
                errorHeaders = {
                    abc: 123
                };

                request('/foo/bar', {error: errorSpy});
                //execute error response from host
                this.invokeErrorRequestResponse( [{headers: errorHeaders}, {}, {}] );

                equal(errorSpy.args[0][0].getAllResponseHeaders(), 'abc: 123', 'all error headers are returned');
            });

            test("error callback getResponseHeader returns the response header", function () {
                var errorSpy = sinon.spy(),
                errorHeaders = {
                    abc: 123
                };

                request('/foo/bar', {error: errorSpy});
                //execute error response from host
                this.invokeErrorRequestResponse( [{headers: errorHeaders}, {}, {}] );

                equal(errorSpy.args[0][0].getResponseHeader('abc'), 123, 'correct value is returned for header');

            });

        });
    });

})();
