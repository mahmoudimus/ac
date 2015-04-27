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

    // Helper method to check the round trip and matching of a pattern and a link
    // pattern - the url pattern to define in the definition
    // link - the link that is being pasted
    // shouldMatch - true: the link should have matched, or false: if the link should not have matched
    function quickRoundTripCheck(pattern, link, shouldMatch) {
        var autoconvertDef = {
            "macroName": "macro", "autoconvert": {"urlParameter": "url"},
            "matcherBean": {"pattern": pattern}
        };

        // if the pattern is not a valid pattern, it does not match
        if (!Autoconvert.isValidAutoconvertDef(autoconvertDef)) {
            ok(!shouldMatch);
            return;
        }

        autoconvertDef.matcherBean.pattern = Autoconvert.convertPatternToRegex(pattern);

        Autoconvert.factory(autoconvertDef, function (macro, done) {
            // has matched
            ok(shouldMatch);
            strictEqual(macro.params.url, link);
        })({source: link}, undefined, function () {
            // has not matched
            ok(!shouldMatch);
        });
    }

    test("Replace all test", function () {
        var s = "aaaaabbb";
        var replaced = Autoconvert.replaceAll("a", "b", s);
        strictEqual(replaced, "bbbbbbbb", "Replace all should replace all a's with b's");
    });

    test("wildcard combination tests", function () {
        quickRoundTripCheck("http://example.com/{}", "http://example.com/12345", true);
        quickRoundTripCheck("http://example.com/", "http://example.com/12345", false);
        quickRoundTripCheck("http://{}/example.com", "http://example.com", false);
        quickRoundTripCheck("http://example.com/{}{}{}/", "http://example.com/12345/", true);
        quickRoundTripCheck("http://example.com/{}/", "http://example.com/12345/6789/", false);
        quickRoundTripCheck("http://example.com/{}/{}/", "http://example.com/12345/6789/", true);
        quickRoundTripCheck("http://example.com/{}{}", "http://example.com/12345", true);
    });

    test("adjacent wildcard tests", function () {
        var patternOne = "http://example.com/{}{}/";
        var patternTwo = "http://example.com/{}/{}{}{}{}{}/";

        var regexPatternOne = Autoconvert.convertPatternToRegex(patternOne);
        var regexPatternTwo = Autoconvert.convertPatternToRegex(patternTwo);

        strictEqual(regexPatternOne, "^http:\\/\\/example\\.com\\/[^/]*?\\/$");
        strictEqual(regexPatternTwo, "^http:\\/\\/example\\.com\\/[^/]*?\\/[^/]*?\\/$");
    });

    test("blacklisted patterns test", function () {
        quickRoundTripCheck("http://{}", "http://23456", false);
        quickRoundTripCheck("{}", "http://facebook.com", false);
    });

    test("handlers registered test", function () {
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

    test("security test", function () {
        // set up the autoconvert defs to test
        var autoconvertDefs = [
            {
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {"pattern": "http://example.com/<script>alert('hi there')</script>"}
            }
        ];

        var handlerSpy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers(autoconvertDefs, tinymce);
        ok(handlerSpy.calledOnce);

        quickRoundTripCheck("http://example.com/<script>alert('hi there')</script>", "http://example.com/<script>alert('hi there')</script>", true);
    });
});
