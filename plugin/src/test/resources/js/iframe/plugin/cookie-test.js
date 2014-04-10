var xdmMockCookie;
(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin',
        map: {
            '*': {
                '_xdm': '_xdmMockCookieTest'
            }
        },
        paths: {
            '_xdmMockCookieTest': '/base/src/test/resources/js/iframe/plugin/_xdmMockCookieTest'
        }
    });

    xdmMockCookie = {
        init: function(){},
        saveCookie: sinon.spy(),
        readCookie: sinon.spy(),
        eraseCookie: sinon.spy()
    };

    context(["_rpc", "cookie"], function(_rpc, cookie){
        _rpc.init();

        module("Cookie plugin", {
            setup: function(){
                xdmMockCookie.saveCookie.reset();
                xdmMockCookie.readCookie.reset();
                xdmMockCookie.eraseCookie.reset();
            }
        });

        test('save calls remove.saveCookie', function(){
            cookie.save();
            ok(xdmMockCookie.saveCookie.calledOnce);
        });

        test("save passes cookie name as first parameter", function(){
            var cookieName = "mycookiename";
            cookie.save(cookieName);
            equal(xdmMockCookie.saveCookie.args[0][0], cookieName);
        });
        
        test("save passes cookie value as the second parameter", function(){
            var cookieValue = "abc123";
            cookie.save("somename", cookieValue);
            equal(xdmMockCookie.saveCookie.args[0][1], cookieValue);
        });

        test("save passes cookie expiry as the third parameter", function(){
            var cookieExpiry = 1;
            cookie.save("somename", "cookieValue", cookieExpiry);
            equal(xdmMockCookie.saveCookie.args[0][2], cookieExpiry);
        });

        test('read calls remove.readCookie', function(){
            cookie.read();
            ok(xdmMockCookie.readCookie.calledOnce);
        });

        test("read passes cookie name as first parameter", function(){
            var cookieName = "mycookie";
            cookie.read(cookieName);
            equal(xdmMockCookie.readCookie.args[0][0], cookieName);
        });

        test("read passes a callback as the second parameter", function(){
            var callback = function(){};
            cookie.read("somecookie", callback);
            deepEqual(xdmMockCookie.readCookie.args[0][1], callback);
        });


        test('erase calls remove.eraseCookie', function(){
            cookie.erase();
            ok(xdmMockCookie.eraseCookie.calledOnce);
        });

        test("erase passes cookie name as first parameter", function(){
            var cookieName = "acookiename";
            cookie.erase(cookieName);
            equal(xdmMockCookie.eraseCookie.args[0][0], cookieName);
        });

    });

})();
