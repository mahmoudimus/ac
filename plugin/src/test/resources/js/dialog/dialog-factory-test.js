define(['dialog/dialog-factory'], function() {

    _AP.require(["dialog/dialog-factory"], function(dialogFactory) {

        module("Dialog Factory", {
        });

        test("open a dialog by key", function(){
            dialogFactory({
                key: "abc",
                moduleKey: "moduleabc"
            }, {}, "");
        });


        test("open a dialog by url", function(){
            dialogFactory({
                key: "somekey",
                url: "/dialog.html"
            }, {}, "");
        });

        test("")

    });

});
