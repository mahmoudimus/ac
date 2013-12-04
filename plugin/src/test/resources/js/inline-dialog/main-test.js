(function(){
    define(['inline-dialog/main'], function() {

        _AP.require(["inline-dialog"], function(inlineDialog) {

            var INLINE_DIALOG_SELECTOR = '.aui-inline-dialog';

            module("Inline Dialog Main", {
                setup: function() {
                    AJS.contextPath = function() { return ""; };
                    $content = $('<div class="' + INLINE_DIALOG_SELECTOR + '"><div class="ap-content"></div></div>');
                    $('<div id="qunit-fixture" />').append($content).appendTo('body');
                },
                teardown: function() {
                    $('#qunit-fixture').remove();
                }
            });

            test("Show inline dialog shows the inline dialog", function() {
                $(".ap-content").append('<div class="contents"><a href="#" class="trigger"></a></div>');
                var mock = {
                    show: sinon.spy()
                };
                $(".contents").data("inlineDialog", mock);

                inlineDialog.showInlineDialog($(".trigger"));

                ok(mock.show.calledOnce, "data bound inline dialog show method is called");
            });

        });
    });
})();
