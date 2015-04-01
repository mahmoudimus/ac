WRM = {};
WRM.data = {};
WRM.data.claim = function() {
    return {};
};

define(['ac/confluence/macro/autoconvert'], function(Autoconvert) {

   module("Autoconvert tests", {
       setup: function() {
           tinymce = {};
           tinymce.plugins = {};
           tinymce.plugins.Autoconvert = {};
           tinymce.plugins.Autoconvert.autoConvert = {};
           tinymce.plugins.Autoconvert.autoConvert.addHandler = function() {};
           tinymce.plugins.Autoconvert.convertMacroToDom = function() {};
       },
       teardown: function() {

       }
   });

    test("Replace all", function() {
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

    test("factory test", function() {

        // set up the autoconvert defs to test
        WRM.data.claim = function() {
            a1 = {
                "macroName": "macro a",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean":
                    { "pattern": "http:/{}/example.com/" }
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

        // test the defined autoconverts
        var spy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers();
        ok(spy.calledTwice);
    });
});


// To test: wildcard combinations
// "http:/{}/example.com/"
// "http://example.com/{}{}{}/"
// "http://example.com/{}{}"
// "http://example.com/{}/{}/{}/{}/{}/{}"

// To test: dangerous looking security ones
// "alert('hi there')"
// "'"
// "(.*)"
// "^(\w+\d+)+a$"

// To test: super greedy ones
// "http://{}"
// "{}"