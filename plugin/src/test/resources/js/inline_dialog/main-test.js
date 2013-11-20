define(['inline_dialog/main'], function() {

    _AP.require(["inline-dialog"], function(inlineDialog) {

        module("Inline Dialog Main", {
            setup: function() {
                AJS.contextPath = function() { return ""; };
                $content = $('<div class="aui-inline-dialog"><div class="ap-content"></div></div>');
                $('<div id="qunit-fixture" />').append($content).appendTo('body');
            },
            teardown: function() {
                $('#qunit-fixture').remove();
            }
        });

        test("hideInlineDialog exists", function() {
            ok(inlineDialog.hideInlineDialog);
        });

        test("hideInlineDialog hides the inlineDialog", function() {
            inlineDialog.hideInlineDialog($('.ap-content'));
            ok(!$('.aui-inline-dialog').is(":visible"));
        });

        test("showInlineDialog exists", function() {
            ok(inlineDialog.showInlineDialog);
        });

        test("showInlineDialog shows the inlineDialog", function() {
            ok($('.aui-inline-dialog').is(":visible"));
            $('.aui-inline-dialog').hide();
            inlineDialog.showInlineDialog($('.ap-content'));
            ok($('.aui-inline-dialog').is(":visible"));
        });


    });

});
