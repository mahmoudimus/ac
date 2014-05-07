define(['dialog/dialog-factory'], function() {

    _AP.require(["dialog/dialog-factory"], function(dialogFactory) {

        module("Dialog Factory", {
            setup: function(){
                this.dialogSpy = {
                    show: sinon.spy(),
                    on: sinon.spy(),
                    remove: sinon.spy(),
                    hide: sinon.spy()
                };
                this.layerSpy = {
                    changeSize: sinon.spy()
                };

                AJS.dialog2 = sinon.stub().returns(this.dialogSpy);
                AJS.layer = sinon.stub().returns(this.layerSpy);
                this.server = sinon.fakeServer.create();
                AJS.contextPath = sinon.stub().returns("");

            },
            teardown: function(){
                this.server.restore();
                // clean up mock
                _AP.AJS = null;
                AJS.dialog2 = null;
                AJS.layer = null;
                AJS.contextPath = null;
            }
        });

        test("open a dialog by key launches an xhr", function(){
            this.server.respondWith("GET", /.*somekey\/somemodulekey/,
            [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

            dialogFactory({
                key: 'somekey',
                moduleKey: 'somemodulekey',
                id: 'dialogid'
            },
            {}, "");

            equal(AJS.dialog2.args[0][0].attr('id'), "dialogid", "Dialog element was created");
            ok(this.dialogSpy.show.calledOnce, "Dialog was shown");

        });

        test("open a dialog by url launches an xhr", function(){
            this.server.respondWith("GET", /.*render\-signed\-iframe/,
            [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

            dialogFactory({
                key: "somekey",
                url: "/dialog.html",
                id: 'dialogid'
            }, {}, "");

            equal(AJS.dialog2.args[0][0].attr('id'), "dialogid", "Dialog element was created");
            ok(this.dialogSpy.show.calledOnce, "Dialog was shown");
            ok(this.server.requests[0].url.search(/remote-url\=%2Fdialog\.html/) > 0, "request url contains the remote url to sign");
            ok(this.server.requests[0].url.search(/plugin-key\=somekey/) > 0, "request url contains the plugin key");
        });

    });

});
