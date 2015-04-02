define(['ac/confluence/macro/autoconvert'], function (Autoconvert) {

    module("Autoconvert tests", {
        setup: function () {
            tinymce = {};
            tinymce.plugins = {};
            tinymce.plugins.Autoconvert = {};
            tinymce.plugins.Autoconvert.autoConvert = {};
            tinymce.plugins.Autoconvert.autoConvert.addHandler = function () {
            };
            tinymce.plugins.Autoconvert.convertMacroToDom = function () {
            };

            WRM = {};
            WRM.data = {};
            WRM.data.claim = function () {
                return {};
            };
        },
        teardown: function () {

        }
    });

    test("Replace all test", function () {
        var s = "aaaaabbb";
        var replaced = Autoconvert.replaceAll("a", "b", s);
        strictEqual(replaced, "bbbbbbbb", "Replace all should replace all a's with b's");
    });
    //
    //test("registerAutoconvertHandlers test", function () {
    //    var spy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
    //    Autoconvert.registerAutoconvertHandlers([{}, {}], tinymce);
    //    ok(spy.calledTwice);
    //});

    test("wildcard combinations test", function () {

        // set up the autoconvert defs to test
        var autoconvertDefs = [
                {
                    "macroName": "macro a",
                    "autoconvert": {
                        "urlParameter": "url"
                    },
                    "matcherBean": {"pattern": "http://example.com/{}/{}/{}/{}/{}/{}"}
                },
                {
                    "macroName": "macro b",
                    "autoconvert": {
                        "urlParameter": "url"
                    },
                    "matcherBean": {"pattern": "http://example.com/{}{}{}/"}
                }
            ];

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers(autoconvertDefs, tinymce);
        ok(handlerSpy.calledTwice);

    });

    test("factory test", function () {
        Autoconvert.factory({
            "macroName": "macro a",
            "autoconvert": {
                "urlParameter": "url"
            },
            "matcherBean": {"pattern": "http://example.com/<script>alert('hi there')</script>"}
        }, function(macro, done) {
            // assert on the macro object
        });
    });

    test("security tests", function () {

        // set up the autoconvert defs to test

        var autoconvertDefs = [{
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {"pattern": "http://example.com/<script>alert('hi there')</script>"}
            },
                {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {"pattern": "http://(.*)"}
            }, {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {"pattern": "http://^(\w+\d+)+a$"}
            } ];

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers(autoconvertDefs, tinymce);
        ok(handlerSpy.calledThrice);
    });

    test("greedy tests", function () {

        // set up the autoconvert defs to test

        var autoconvertDefs = [{
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {"pattern": "http://{}"}
            },
            a2 = {
                "macroName": "macro b",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {"pattern": "{}"}
            }];

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers(autoconvertDefs, tinymce);
        ok(handlerSpy.calledTwice);
    });
});


// To test: wildcard combinations
// "http:/{}/example.com/"
// "http://example.com/{}{}{}/"
// "http://example.com/{}{}"
