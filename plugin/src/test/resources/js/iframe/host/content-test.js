(function(){
    define(["iframe/host/content"], function() {
        _AP.require(["host/content"], function(contentUtilities){

            var pluginKey = "foo-plugin-key",
            capability = {
                key: "bar-capability-key"
            },
            productContextJson = "",
            contentPath = "/plugins/servlet/atlassian-connect/" + pluginKey + "/" + capability.key;

            module("Content Utilities", {
                setup: function() {
                    AJS.contextPath = function(){ return "https://www.example.com"; };
                    this.container = $("<div />").attr("id", "qunit-container").appendTo("body");
                    this.server = sinon.fakeServer.create();
                    this.server.respondWith("GET", new RegExp(".*" + contentPath + ".*"),
                        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

                    $('<a id="qunit-fixture" href="http://foo.com/?width=101&height=200" />').appendTo('body');
                },
                teardown: function() {
                    this.container.remove();
                    delete AJS.contextPath;
                    this.server.restore();
                    $('#qunit-fixture').remove();
                },
            });

            test("getIframeHTMLForKey requests the correct path", function(){
                var ajaxRequest = contentUtilities.getIframeHtmlForKey(pluginKey, productContextJson, capability);

                equal(0, this.server.requests[0].url.search(new RegExp("https://www.example.com/plugins/servlet/atlassian-connect/foo-plugin-key/bar-capability-key")));
            });

            test("getIframeHTMLForKey returns an ajax request", function(){
                var ajaxRequest = contentUtilities.getIframeHtmlForKey(pluginKey, productContextJson, capability);

                ok(ajaxRequest.readyState);
            });

            test("eventHandler assigns an event handler to the dom node", function(){
                var spy = sinon.spy();

                contentUtilities.eventHandler("click", '#qunit-fixture', spy);
                $('#qunit-fixture').trigger('click');

                ok(spy.calledOnce);
            });

            test("eventHandler callback includes the width", function(){
                var spy = sinon.spy();

                contentUtilities.eventHandler("click", '#qunit-fixture', spy);
                $('#qunit-fixture').trigger('click');

                equal(spy.firstCall.args[1]['width'], '101');
            });

            test("eventHandler callback includes the height", function(){
                var spy = sinon.spy();

                contentUtilities.eventHandler("click", '#qunit-fixture', spy);
                $('#qunit-fixture').trigger('click');

                equal(spy.firstCall.args[1]['height'], '200');
            });


        });



    });

})();
