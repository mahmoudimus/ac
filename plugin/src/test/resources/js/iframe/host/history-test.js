(function(){
    define(["iframe/host/history"], function() {
        _AP.require(["host/history"], function(connectHistory){

            module("History", {
                setup: function() {
                },
                teardown: function() {
                    window.location.replace("#"); //reset the anchor.
                }
            });


            test("pushState updates the url of the page", function(){
                connectHistory.pushState("mystate");
                ok(window.location.hash.match(/^\#\!mystate$/));
            });

            test("pushState adds an additional entry to the browser history", function(){
                var lengthBefore = window.history.length;
                connectHistory.pushState("foo");
                equal(window.history.length, lengthBefore +1);
            });

            test("replaceState updates the url of the page", function(){
                connectHistory.replaceState("myreplacedstate");
                ok(window.location.hash.match(/^\#\!myreplacedstate$/));
            });

            test("replaceState does not add to the length of the browser history object", function(){
                var lengthBefore = window.history.length;
                connectHistory.replaceState("bar");
                equal(window.history.length, lengthBefore);
            });

            test("hashchange invokes the callback when the url changes", function(){
                var dummyEvent = {
                    newURL: "http://www.google.com/#!abc",
                    oldURL: "http://www.google.com/#!foobar"
                },
                callback = sinon.spy();
                connectHistory.hashChange(dummyEvent, callback);
                ok(callback.calledOnce);
            });

            test("hashchange is ignored when the url does not change", function(){
                var dummyEvent = {
                    newURL: "http://www.google.com/#!abc",
                    oldURL: "http://www.google.com/#!abc"
                },
                callback = sinon.spy();
                connectHistory.hashChange(dummyEvent, callback);
                equal(callback.calledOnce, false);
            });

            test("hashchange is ignored when the url is changed by pushState", function(){
                connectHistory.pushState("foobar");
                var dummyEvent = {
                    newURL: "http://www.google.com/#!foobar",
                    oldURL: "http://www.google.com/#!abc"
                },
                callback = sinon.spy();
                connectHistory.hashChange(dummyEvent, callback);
                equal(callback.calledOnce, false);
            });

            test("hashchange is ignored when the url is changed by replaceState", function(){
                connectHistory.replaceState("foobar");
                var dummyEvent = {
                    newURL: "http://www.google.com/#!foobar",
                    oldURL: "http://www.google.com/#!abc"
                },
                callback = sinon.spy();
                connectHistory.hashChange(dummyEvent, callback);
                equal(callback.calledOnce, false);
            });

            test("getState returns the current url anchor without prefix", function(){
                var state = "foobar";
                connectHistory.pushState(state);
                equal(connectHistory.getState(), state);
            });


        });
    });

})();
