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

    // Helper method to check the round trip and matching of a pattern and a link
    // pattern - the url pattern to define in the definition
    // link - the link that is being pasted
    // shouldMatch - true: the link should have matched, or false: if the link should not have matched
    function quickRoundTripCheck(pattern, link, shouldMatch) {
        Autoconvert.factory({
            "macroName": "macro", "autoconvert": {"urlParameter": "url"},
            "matcherBean": {"pattern": pattern}
        }, function (macro, done) {
            // has matched
            ok(shouldMatch)
            strictEqual(macro.params.url, link);
        })({source: link}, undefined, function () {
            // has not matched
            ok(!shouldMatch)
        });
    }

    // To test: wildcard combinations
    // "http:/{}/example.com/"
    // "http://example.com/{}{}{}/"
    // "http://example.com/{}{}"

    test("round trip tests", function () {
        quickRoundTripCheck("http://example.com/{}", "http://example.com/12345", true);
        quickRoundTripCheck("http://example.com/", "http://example.com/12345", false);
        quickRoundTripCheck("http://{}/example.com", "http://example.com", false);
        quickRoundTripCheck("http://example.com/{}{}{}/", "http://example.com/12345/", true);
        quickRoundTripCheck("http://example.com/{}/", "http://example.com/12345/6789/", false);
        quickRoundTripCheck("http://example.com/{}/{}/", "http://example.com/12345/6789/", true);
        quickRoundTripCheck("http://example.com/{}{}", "http://example.com/12345", true);
    });

    test("security tests", function () {
        // set up the autoconvert defs to test
        var autoconvertDefs = [
            {
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
                "matcherBean": {"pattern": "http://^(\w+\d+)+a$"}
            }];

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers(autoconvertDefs, tinymce);
        ok(handlerSpy.calledThrice);
    });
});
