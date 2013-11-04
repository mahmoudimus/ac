(function(){

    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin',

        map: {
            '*': {
                '_xdm': '_xdmMockEnvTest'
            }
        }
    });

    var xdmMock = {
        init: function() {},
        getLocation: sinon.spy(),
        getUser: sinon.spy(),
        getTimeZone: sinon.spy(),
        showMessage: sinon.spy(),
        clearMessage: sinon.spy(),
        resize: sinon.spy(),
        sizeToParent: sinon.spy()
    };

    define('_xdmMockEnvTest', function () {
        return function() {
            return xdmMock;
        };
    });
    context(["_rpc", "env", "_dollar"], function(_rpc, env, $) {
        _rpc.init();

        module("Env plugin", {
            setup: function() {
                this.createFixtureContainer();

                this.clock = sinon.useFakeTimers();
                xdmMock.getLocation.reset();
                xdmMock.getUser.reset();
                xdmMock.getTimeZone.reset();
                xdmMock.showMessage.reset();
                xdmMock.clearMessage.reset();
                xdmMock.resize.reset();
                xdmMock.sizeToParent.reset();
            },
            createFixtureContainer: function(){
                this.container = document.createElement('div');
                this.container.id = 'qunit-container';
                this.container.innerHTML =
                '<meta name="ap-local-base-url" content="http://www.example.com/confluence" />' +
                '<meta name="ap-fixture" content="foo bar" />';
                document.body.appendChild(this.container);
            },
            createAcContainer: function(opts){
                var acContainer = document.createElement('div');
                $.extend(acContainer, opts);
                this.container.appendChild(acContainer);

                return acContainer;
            },
            teardown: function() {
                this.clock.restore();
                document.body.removeChild(this.container);
            },
        });


        test("meta", function() {
            equal(env.meta('fixture'), 'foo bar');
        });

        test("#content container found", function() {
            var acContainer = this.createAcContainer({
                'id': 'content',
                'innerHTML': '<span>foo bar</span>'
            });
            equal(env.container().innerHTML, acContainer.innerHTML);
        });

        test(".ac-content container found", function() {
            var acContainer = this.createAcContainer({
                'className': 'ac-content',
                'innerHTML': '<span>foo bar</span>'
            });

            equal(env.container().innerHTML, acContainer.innerHTML);
        });

        test("container not found", function() {
            equal(env.container().nodeName, 'BODY');
        });

        test("localUrl", function() {
            equal(env.localUrl(), 'http://www.example.com/confluence');
        });

        test("localUrl appends the path", function() {
            equal(env.localUrl('/abc'), 'http://www.example.com/confluence/abc');
        });

        test("size gets correct height", function() {
            var height = 50,
            acContainer = this.createAcContainer({
                'className': 'ac-content',
                'innerHTML': '<span>foo bar</span>',
            });
            acContainer.style.height = height + 'px';
            dim = env.size(null, null, acContainer);

            equal(dim.h, height);

        });

        test("auto resize causes 100% width to be returned", function() {
            var width = 50,
            acContainer = this.createAcContainer({
                'className': 'ac-content',
                'innerHTML': '<span>foo bar</span>',
            });
            acContainer.style.width = width + 'px';
            dim = env.size(null, null, acContainer);

            equal(dim.w, "100%");

        });

        test("size returns passed width", function() {
            var width = 50,
            acContainer = this.createAcContainer({
                'className': 'ac-content',
                'innerHTML': '<span>foo bar</span>',
            });
            dim = env.size(width, null, acContainer);
            equal(dim.w, width);

        });

        test("size returns passed height", function() {
            var height = 50,
            acContainer = this.createAcContainer({
                'className': 'ac-content',
                'innerHTML': '<span>foo bar</span>',
            });
            dim = env.size(null, height, acContainer);
            equal(dim.h, height);
        });

        //xdm bridge methods.
        test("getLocation calls remote getLocation", function() {
            env.getLocation();
            ok(xdmMock.getLocation.calledOnce);
        });

        test("getLocation passes callback to remote method", function() {
            var spy = sinon.spy();
            env.getLocation(spy);
            equal(xdmMock.getLocation.args[0][0], spy);
        });

        test("getUser calls remote getUser", function() {
            env.getUser();
            ok(xdmMock.getUser.calledOnce);
        });

        test("getUser passes callback to remote method", function() {
            var spy = sinon.spy();
            env.getUser(spy);
            equal(xdmMock.getUser.args[0][0], spy);
        });

        test("getTimeZone calls remote getTimeZone", function() {
            env.getTimeZone();
            ok(xdmMock.getTimeZone.calledOnce);
        });

        test("getTimeZone passes callback to remote method", function() {
            var spy = sinon.spy();
            env.getTimeZone(spy);
            equal(xdmMock.getTimeZone.args[0][0], spy);
        });

        test("showMessage calls remote showMessage", function() {
            env.showMessage();
            ok(xdmMock.showMessage.calledOnce);
        });

        test("showMessage passes id to remote method", function() {
            var id = "foo";
            env.showMessage(id);
            equal(xdmMock.showMessage.args[0][0], id);
        });

        test("showMessage passes title to remote method", function() {
            var title = "foo title";
            env.showMessage(null, title);
            equal(xdmMock.showMessage.args[0][1], title);
        });

        test("showMessage passes body to remote method", function() {
            var body = "foo body";
            env.showMessage(null, null, body);
            equal(xdmMock.showMessage.args[0][2], body);
        });

        test("clearMessage calls remote clearMessage", function() {
            env.clearMessage();
            ok(xdmMock.clearMessage.calledOnce);
        });

        test("clearMessage passes id to remote method", function() {
            var id = "bar";
            env.clearMessage(id);
            equal(xdmMock.clearMessage.args[0][0], id);
        });

        test("resize calls remote resize", function() {
            env.resize();
            //resize runs every 50ms.
            this.clock.tick(50);
            ok(xdmMock.resize.calledOnce);
        });

        test("resize calls remote resize with width", function() {
            var width = 20;
            env.resize(width);
            //resize runs every 50ms.
            this.clock.tick(50);
            equal(xdmMock.resize.args[0][0], width);
        });

        test("resize calls remote resize with height", function() {
            var height = 23;
            env.resize(null, height);
            //resize runs every 50ms.
            this.clock.tick(50);
            equal(xdmMock.resize.args[0][1], height);
        });

        test("sizeToParent calls remote sizeToParent", function() {
            env.sizeToParent();
            //sizeToParent runs every 50ms.
            this.clock.tick(50);
            ok(xdmMock.sizeToParent.calledOnce);
        });


    });

})();
