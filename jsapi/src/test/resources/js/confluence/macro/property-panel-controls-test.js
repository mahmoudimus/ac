define(['ac/confluence/macro/property-panel-controls'], function (PropertyPanelControls) {

    var testAddonControls = [{
        "type":"button",
        "key":"ok",
        "name": {"value":"Save"}
    }];

    var controlsDataProviderResponse = {
        "test-addon": {
            "test-dynamic-macro": testAddonControls
        },
        "some-addon": {},
        "another-test-addon": {
            "another-macro-with-controls":
                [{
                    "type":"button",
                    "key":"charlie-button",
                    "name": {
                        "value":"Let's Charlie",
                        "i18n":"charlie.button.name"
                    }
                }]
        }
    };

    module("Property Panel Controls tests", {
        setup: function () {
            WRM = {};
            WRM.data = {};
            WRM.data.claim = function () {
                return {};
            };
        },
        teardown: function () {

        }
    });

    test("module returns getControls function", function () {
        equal(typeof PropertyPanelControls("testMacroName").getControls, "function");
    });

    test("data provider failure results in null value", function () {
        var controls = null;
        var callback = function(retrievedControls) {
            equal(controls, null);
        };
        PropertyPanelControls("test-dynamic-macro").getControls(callback);
    });

    test("providing macro name returns controls of that macro", function () {
        WRM.data.claim = function() {
            return controlsDataProviderResponse;
        };
        var callback = function(retrievedControls) {
            equal(retrievedControls, testAddonControls);
        };
        PropertyPanelControls("test-dynamic-macro").getControls(callback);
    });


});
