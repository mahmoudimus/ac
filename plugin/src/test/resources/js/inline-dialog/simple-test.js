define(['inline-dialog/simple'], function() {

    _AP.require(["inline-dialog/simple"], function(simpleInlineDialog) {

        var INLINE_DIALOG_SELECTOR = '.aui-inline-dialog';
        var MOCK_DIALOG_ID = 'foobar';

        module("Inline Dialog Simple", {
            setup: function() {
                var inlineDialogMock = $("<div id='" + MOCK_DIALOG_ID + "'></div>");
                this.server = sinon.fakeServer.create();
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
                this.server.restore();
                this.showPopupMock.reset();
                AJS = {};
                $('#qunit-fixture').remove();
            }
        });


        test("Inline dialog creates an inline dialog", function() {
            var href ="someurl";
            var options = {};
            simpleInlineDialog(href, options);
            ok(AJS.InlineDialog.calledOnce);
        });

        test("Inline dialog create launches an xhr", function() {
            var href ="someurl";
            var options = {};

            this.server.respondWith("GET", href,
                [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

            simpleInlineDialog(href, options);
            this.server.respond();

            equal(href, this.server.requests[0].url);
        });

        test("Inline dialog returns the inline dialog id", function() {
            var href ="someurl";
            var options = {};

            this.server.respondWith("GET", href,
                [200, { "Content-Type": "text/html" }, 'This is the <span id="my-span">content</span>']);

            var inlineDialog = simpleInlineDialog(href, options);
            this.server.respond();
            equal(MOCK_DIALOG_ID, inlineDialog.id);
        });


    });
});
