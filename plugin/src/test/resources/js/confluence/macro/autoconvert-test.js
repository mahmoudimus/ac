define(['ac/confluence/macro/autoconvert'], function(Autoconvert) {

   module("Autoconvert tests", {
       setup: function() {
           tinymce = {};
           tinymce.plugins = {};
           tinymce.plugins.Autoconvert = {};
           tinymce.plugins.Autoconvert.autoConvert = {};
           tinymce.plugins.Autoconvert.autoConvert.addHandler = function() {};
           tinymce.plugins.Autoconvert.convertMacroToDom = function() {};

           WRM = {};
           WRM.data = {};
           WRM.data.claim = function() {
               return {};
           };
       },
       teardown: function() {

       }
   });

    test("Replace all test", function() {
        var s = "aaaaabbb";
        var replaced = Autoconvert.replaceAll("a", "b", s);
        strictEqual(replaced, "bbbbbbbb", "Replace all should replace all a's with b's");
    });

    test("registerAutoconvertHandlers test", function() {
        sinon.stub(WRM.data, "claim").returns([{}, {}]);
        sinon.stub(Autoconvert, "factory").returns(function() {});
        var spy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers();
        ok(spy.calledTwice);
        WRM.data.claim.restore();
        Autoconvert.factory.restore();
    });

    test("wildcard combinations test", function() {

        // set up the autoconvert defs to test
        WRM.data.claim = function() {
            a1 = {
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                    { "pattern": "http://example.com/{}/{}/{}/{}/{}/{}" }
            };

            a2 = {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                { "pattern": "http://example.com/{}{}{}/" }
            };
            return [ a1, a2 ] ;
        };

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        // var buildMacroSpy = sinon.spy(Autoconvert, "buildMacro");
        Autoconvert.registerAutoconvertHandlers();
        ok(handlerSpy.calledTwice);

        // fire the internal paste?
        // ok(buildMacroSpy.calledWith());

    });

    test("security tests", function() {

        // set up the autoconvert defs to test
        WRM.data.claim = function() {
            a1 = {
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                { "pattern": "http://example.com/<script>alert('hi there')</script>" }
            };

            a2 = {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                { "pattern": "http://(.*)" }
            };

            a3 = {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                { "pattern": "http://^(\w+\d+)+a$" }
            };
            return [ a1, a2, a3 ] ;
        };

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        // var buildMacroSpy = sinon.spy(Autoconvert, "buildMacro");
        Autoconvert.registerAutoconvertHandlers();
        ok(handlerSpy.calledThrice);

        // fire the internal paste?
        // ok(buildMacroSpy.calledWith());
    });

    test("greedy tests", function() {

        // set up the autoconvert defs to test
        WRM.data.claim = function() {
            a1 = {
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                { "pattern": "http://{}" }
            };

            a2 = {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                { "pattern": "{}" }
            };

            return [ a1, a2 ] ;
        };

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        // var buildMacroSpy = sinon.spy(Autoconvert, "buildMacro");
        Autoconvert.registerAutoconvertHandlers();
        ok(handlerSpy.calledTwice);

        // fire the internal paste?
        // ok(buildMacroSpy.calledWith());
    });
});


// To test: wildcard combinations
// "http:/{}/example.com/"
// "http://example.com/{}{}{}/"
// "http://example.com/{}{}"
