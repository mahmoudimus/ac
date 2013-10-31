require.config({
    map: {
        '*': {
          '_xdm': '_xdmMock',
        }
    }
});

var xdmMock = {
    init: function() {},
    request: sinon.spy()
};

define('_xdmMock', function () {
    return function() {
        return xdmMock;
    };
});

define(["request", "_rpc"], function(request, _rpc) {

    module("Request Plugin", {
        teardown: function () {
            if(xdmMock.request){
                xdmMock.request.reset();
            }
        },
        isFunction: function(functionToCheck) {
            var getType = {};
            return functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
        },
        invokeSuccessRequestResponse: function(args){
            return xdmMock.request.args[0][1]( args );
        },
        invokeErrorRequestResponse: function(args){
            return xdmMock.request.args[0][2]( args );
        }
    });

    test("invokes to host request", function () {
        request("/foo/bar");

        ok(xdmMock.request.calledOnce, "invokes host request function");
    });

    test("host request is passed url", function () {
        request("/foo/bar");

        equal(xdmMock.request.args[0][0].url, "/foo/bar", "passes correct url");
    });

    test("host request is passed success callback", function () {
        request('/foo/bar');

        ok(this.isFunction(xdmMock.request.args[0][1]));
    });

    test("host request is passed fail callback", function () {
        request('/foo/bar');

        ok(this.isFunction(xdmMock.request.args[0][2]));
    });

    test("host request is passed fail callback", function () {
        request('/foo/bar');

        ok(this.isFunction(xdmMock.request.args[0][2]));
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

    test("callback getAllResponseHeaders returns a formatted string of headers", function () {
        var successSpy = sinon.spy(),
        successHeaders = {
            abc: 123
        };

        request('/foo/bar', {success: successSpy});
        //execute success response from host
        this.invokeSuccessRequestResponse( [{}, {}, {headers: successHeaders}] );

        equal(successSpy.args[0][2].getAllResponseHeaders(), 'abc: 123', 'all success headers are returned');
    });

    test("callback getResponseHeader returns the response header", function () {
        var successSpy = sinon.spy(),
        successHeaders = {
            abc: 123
        };

        request('/foo/bar', {success: successSpy});
        //execute success response from host
        this.invokeSuccessRequestResponse( [{}, {}, {headers: successHeaders}] );

        equal(successSpy.args[0][2].getResponseHeader('abc'), 123, 'correct value is returned for header');

    });


});
