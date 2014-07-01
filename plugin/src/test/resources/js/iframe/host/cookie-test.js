(function(){
    define(["iframe/host/cookie"], function() {
        _AP.require(["host/cookie"], function(cookie){

            var SEPARATOR = '$$';

            module("Cookie", {
                setup: function() {
                    this.default_AJSCookie = window.AJS.Cookie;
                    window.AJS.Cookie = {
                        save: sinon.spy(),
                        read: sinon.spy(),
                        erase: sinon.spy()
                    };
                    cookie.internals.addonKey = "myAddon";
                    cookie.internals.moduleKey = "myModule";
                },
                teardown: function() {
                    window.AJS.Cookie = this.default_AJSCookie;
                    document.cookie = "";
                },
            });

            test("saveCookie calls AJS.Cookie.save", function(){
                cookie.internals.saveCookie('name', 'value', 1);
                ok(window.AJS.Cookie.save.calledOnce);
            });

            test("saveCookie prefixes the cookie with the add-on key", function(){
                var cookieName = "myCookie",
                    cookieValue = "some value";

                cookie.internals.saveCookie(cookieName, cookieValue);
                equal(window.AJS.Cookie.save.args[0][0], cookie.internals.addonKey + SEPARATOR + cookieName);
            });

            test("readCookie calls AJS.Cookie.read", function(){
                cookie.internals.readCookie("something");
                ok(window.AJS.Cookie.read.calledOnce);
            });

            test("readCookie prefixes the cookie with the add-on key", function(){
                var cookieName = "myCookie",
                    cookieValue = "some value";
                    cookie.internals.readCookie(cookieName);
                ok(window.AJS.Cookie.read.args[0][0], cookie.internals.addonKey + SEPARATOR + cookieName);
            });

            test("readCookie runs the callback function", function(){
                var cookieName = "myCookie",
                    cookieValue = "some value",
                    callback = sinon.spy();

                cookie.internals.readCookie(cookieName, callback);
                ok(callback.calledOnce);
            });

            test("eraseCookie calls JS.Cookie.erase", function(){
                cookie.internals.eraseCookie("abc");
                ok(window.AJS.Cookie.erase.calledOnce);
            });

            test("eraseCookie prefixes the cookie with the add-on key", function(){
                var cookieName = "myCookie",
                    cookieValue = "some value";

                cookie.internals.eraseCookie(cookieName);
                equal(window.AJS.Cookie.erase.args[0], cookie.internals.addonKey + SEPARATOR + cookieName);
            });

        });

    });

})();
