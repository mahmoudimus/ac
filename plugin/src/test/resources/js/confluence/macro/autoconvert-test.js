WRM = {};
WRM.data = {};
WRM.data.claim = function() {
    return {};
};
tinymce = {};
tinymce.plugins = {};
tinymce.plugins.Autoconvert = {};
tinymce.plugins.Autoconvert.autoConvert = {};
tinymce.plugins.Autoconvert.autoConvert.addHandler = function() {};

define(['ac/confluence/macro/autoconvert'], function(Autoconvert) {

   module("Autoconvert tests", {
       setup: function() {

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

    test("Wildcard placement", function() {
        WRM.data.claim = function() {
            autoconvertdef = {
                "macroName": "",
                "autoconvert": {
                    "urlParameter": "url"
                },
                "matcherBean": {
                    "pattern": "http:/{}/example.com/",
                    "pattern": "http://example.com/{}{}{}/",
                    "pattern": "http://example.com/{}{}",
                    "pattern": "http://example.com/{}/{}/{}/{}/{}/{}"
                }
            }
            return autoconvertdef;
        };

        sinon.stub(WRM.data, "claim").returns([{}, {}]);
        sinon.stub(Autoconvert, "factory").returns(function() {});
        var spy = sinon.spy(tinymce.plugins.Autoconvert.autoConvert, "addHandler");
        Autoconvert.registerAutoconvertHandlers();
        ok(spy.calledTwice);
        WRM.data.claim.restore();
        Autoconvert.factory.restore();
    })


});

// Super greedy ones
WRM.data.claim = function() {
    autoconvertdef = {
        "macroName": "",
        "autoconvert": {
            "urlParameter": "url"
        },
        "matcherBean": {
            "pattern": "http://{}",
            "pattern": "{}"
        }
    }
    return autoconvertdef;
};

// Dangerous security ones
WRM.data.claim = function() {
    autoconvertdef = {
        "macroName": "",
        "autoconvert": {
            "urlParameter": "url"
        },
        "matcherBean": {
            "pattern": "alert('hi there')",
            "pattern": "'",
            "pattern": "(.*)",
            "pattern": "^(\w+\d+)+a$"
        }
    }
    return autoconvertdef;
};