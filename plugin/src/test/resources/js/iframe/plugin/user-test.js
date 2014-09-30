(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin'
    });

    window.xdmMock = {
        init: function() {},
        getUser: sinon.spy(),
        getTimeZone: sinon.spy(),
    };

    context(["user"], function() {
        AP.require(['user'],function(user){

            xdmMock.getUser = sinon.spy();
            xdmMock.getTimeZone = sinon.spy();

            module("User plugin", {
                setup: function(){
                    xdmMock.getUser.reset();
                    xdmMock.getTimeZone.reset();
                }
            });

            test('getUser calls remote getUser', function(){
                user.getUser();
                ok(xdmMock.getUser.calledOnce);
            });

            test('getTimeZone calls remote getTimeZone', function(){
                user.getTimeZone();
                ok(xdmMock.getTimeZone.calledOnce);
            });

            // Test alternate method of invocation
            test('getUser can be called from AP.getUser', function(){
                AP.getUser();
                ok(xdmMock.getUser.calledOnce);
            });

            // Test alternate method of invocation
            test('getTimeZone can be called from AP.getTimeZone', function(){
                AP.getTimeZone();
                ok(xdmMock.getTimeZone.calledOnce);
            });

            test("getUser passes callback to remote method", function() {
                var spy = sinon.spy();
                user.getUser(spy);
                equal(xdmMock.getUser.args[0][0], spy);
            });

            test("getTimeZone passes callback to remote method", function() {
                var spy = sinon.spy();
                user.getTimeZone(spy);
                equal(xdmMock.getTimeZone.args[0][0], spy);
            });

        });
    });

})();
