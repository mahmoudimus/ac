(function(){

    define(['iframe/_xdm'], function(_rpc, $) {

        _AP.require(["_xdm", "_dollar", "host/main"], function(XdmRpc, $, main) {
            module('XDM host', {
                setup: function() {
                    this.container = $("<div />").attr("id", "qunit-container").appendTo("body");
                },
                teardown: function() {
                    this.container.remove();
                },
                iframeId: function() {
                    return "easyXDM_qunit-container_provider";
                },
                createXdm: function(fixture, props, local, remote){
                    fixture = fixture || 'resize-listener-resize.html';

                    return new XdmRpc($, {
                        remoteKey: 'myremotekey',
                        remote: this.getBaseUrl() + '/base/src/test/resources/fixtures/' + fixture + '?oauth_consumer_key=jira:12345&xdm_e=' + encodeURIComponent(this.getBaseUrl()) + '&xdm_c=testchannel',
                        container: 'qunit-container',
                        channel: 'testchannel',
                        props: props || {}
                    }, {
                        local: local || [],
                        remote: remote || {}
                    });
                },
                getBaseUrl: function(){
                    if (!window.location.origin) {
                        window.location.origin = window.location.protocol+"//"+window.location.host;
                    }
                    return window.location.origin;
                }
            });

            test('iframe is created at the specified width', function () {
                this.createXdm("blank.html", { width:10, height: 11});

                equal($("iframe#" + this.iframeId())[0].offsetWidth, 10, "iframe starts at 10px wide");
            });

            test('iframe is created at the specified height', function () {
                this.createXdm("blank.html", { width:10, height: 11});

                equal($("iframe#" + this.iframeId())[0].offsetHeight, 11, "iframe starts at 11px high");
            });

            asyncTest('AP.resize crosses the bridge', function () {
                var spy = function(){
                    ok(true, 'resize was called in the bridge');
                    start();
                };
                this.createXdm(null, null, {resize: spy});
            });

            test('resize function is called when the iframe contents change dimensions', function () {
                stop();
                var spy = sinon.spy(),
                xdm = this.createXdm('resize-listener-autoresize.html', {width:10, height:11}, {
                    resize: spy
                });

                xdm.events.on('resized', function(e){
                    equal(spy.callCount, 2, 'resize function was called twice');
                    start();
                });
            });

        });

    });
})();
