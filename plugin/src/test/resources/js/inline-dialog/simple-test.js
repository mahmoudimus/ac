(function(){
    define(['inline-dialog/simple'], function() {

        _AP.require(["inline-dialog/simple"], function(simpleInlineDialog) {

            var INLINE_DIALOG_SELECTOR = '.aui-inline-dialog';

            module("Inline Dialog Simple", {
                setup: function() {
                    if (!_AP.create) {
                        _AP.require(["host/main"], function(main) {
                            _AP.create = main;
                        });
                        this.apCreateMock = true;
                    }

                    var inlineDialogMock = $("<div></div>");
                    AJS.contextPath = function() { return ""; };
                    $content = $('<div class="' + INLINE_DIALOG_SELECTOR + '"><div class="ap-content"></div></div>');
                    $('<div id="qunit-fixture">').append($content).appendTo('body');

                    this.showPopupMock = sinon.spy();
                    AJS.InlineDialog = sinon.stub().yields(
                        inlineDialogMock,
                        null,
                        this.showPopupMock)
                    .returns(inlineDialogMock);
                },
                teardown: function() {
                    //restore _AP.create to it's default state.
                    if(this.apCreateMock){
                        delete _AP.create;
                    }
                    this.showPopupMock.reset();
                    AJS.InlineDialog = null;
                    $('#qunit-fixture').remove();
                }
            });


            test("Inline dialog creates an inline dialog", function() {
                var href ="someurl";
                var options = {};
                simpleInlineDialog(href, options);
                ok(AJS.InlineDialog.calledOnce);
            });

            test("Inline dialog returns the inline dialog id", function() {
                var href ="someurl";
                var options = {};

                var inlineDialog = simpleInlineDialog(href, options);
                ok(inlineDialog.id.length > 1);
            });


        });
    });
})();
