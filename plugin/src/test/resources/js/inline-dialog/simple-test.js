(function(){
    define(['inline-dialog/simple'], function() {

        _AP.require(["inline-dialog/simple", "_dollar"], function(simpleInlineDialog, $) {

            var INLINE_DIALOG_SELECTOR = '.aui-inline-dialog';

            module("Inline Dialog Simple", {
                setup: function() {
                    if (!_AP.create) {
                        _AP.require(["host/main"], function(main) {
                            _AP.create = main;
                        });
                        this.apCreateMock = true;
                    }

                    var inlineDialogMock = $('<div id="ap-acmodule-foo"></div>');
                    AJS.contextPath = function() { return ""; };
                    $content = $('<div class="' + INLINE_DIALOG_SELECTOR + '"><div class="ap-content"></div></div>');
                    $('<div id="qunit-fixture">').append($content).appendTo('body');

                    this.showPopupMock = sinon.spy();
                    AJS.InlineDialog = sinon.stub().yields(
                        inlineDialogMock,
                        $('<a class="ap-plugin-key-addon ap-module-key-addon__module">link</a>'),
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
                var href = "someurl";
                var options = {
                    bindTo: $("<div id='acmodule-foo' class='ap-inline-dialog'></div>")
                };
                simpleInlineDialog(href, options);
                ok(AJS.InlineDialog.calledOnce);
            });

            test("Inline dialog returns the inline dialog id", function() {
                var href = "someurl";
                var options = {
                    bindTo: $("<div id='irrelevent_id' class='ap-inline-dialog'></div>")
                };

                var inlineDialog = simpleInlineDialog(href, options);
                equal(inlineDialog.id, "ap-acmodule-foo");
            });

            test("Inline dialog bails if no element to bind to", function() {
                var options = {
                };
                ok(!simpleInlineDialog("someurl", options));
            });

            test("Inline dialog bails if bind target is not a jQuery object", function() {
                var options = {
                    bindTo: $("<div id='acmodule-foo' class='ap-inline-dialog'></div>")[0]
                };
                ok(!simpleInlineDialog("someurl", options));
            });

            test("Inline dialog bails if web-item ID is not found", function() {
                var options = {
                    bindTo: $("<div class='ap-inline-dialog'></div>")
                };
                ok(!simpleInlineDialog("someurl", options));
            });
        });
    });
})();
