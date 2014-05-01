(function(){
    define(["iframe/host/content"], function() {
        _AP.require(["host/content"], function(contentUtilities){

            var pluginKey = "foo-plugin-key",
            capability = {
                key: "bar-capability-key"
            },
            productContextJson = "",
            contentPath = "/plugins/servlet/ac/" + pluginKey + "/" + capability.key;

            module("Content Utilities", {
                setup: function() {
                    AJS.contextPath = function(){ return "https://www.example.com"; };
                    this.container = $("<div />").attr("id", "qunit-container").appendTo("body");
                    this.server = sinon.fakeServer.create();
                    this.server.respondWith("GET", new RegExp(".*" + contentPath + ".*"),
                        [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

                    $('<a id="qunit-fixture" href="http://foo.com/?width=101&height=200&cp=%2Fconfluence" class="ap-plugin-key-my-plugin ap-module-key-my-module" />').appendTo('body');
                },
                teardown: function() {
                    this.container.remove();
                    delete AJS.contextPath;
                    this.server.restore();
                    $('#qunit-fixture').remove();
                },
            });

            test("getContentUrl returns the correct contentURL", function(){
                var expectedBeginningUrl = new RegExp("https://www.example.com/plugins/servlet/ac/foo-plugin-key/bar-capability-key"),
                url = contentUtilities.getContentUrl(pluginKey, capability);

                equal(0, url.search(expectedBeginningUrl));
            });

            // ACDEV-590
            test("getContentUrl returns encoded urls", function(){
                var key = "../rest/activity-stream/1.0/i18n/key/<img%20src=x%20onerror=alert(0)>",
                capability = {
                    key: "../rest/activity-stream/1.0/i18n/key/<img%20src=x%20onerror=alert(0)>"
                },
                expectedUrl = 'https://www.example.com/plugins/servlet/ac/..%2Frest%2Factivity-stream%2F1.0%2Fi18n%2Fkey%2F%3Cimg%2520src%3Dx%2520onerror%3Dalert(0)%3E/..%2Frest%2Factivity-stream%2F1.0%2Fi18n%2Fkey%2F%3Cimg%2520src%3Dx%2520onerror%3Dalert(0)%3E',
                url = contentUtilities.getContentUrl(key, capability);
                equal(url, expectedUrl);
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

            test("eventHandler callback includes the context path", function(){
                var spy = sinon.spy();

                contentUtilities.eventHandler("click", '#qunit-fixture', spy);
                $('#qunit-fixture').trigger('click');

                equal(spy.firstCall.args[1]['cp'], '/confluence');

            });

        });



    });

})();
