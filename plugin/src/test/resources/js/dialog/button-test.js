define(['dialog/button'], function() {

    _AP.require(["dialog/button"], function(dialogButton) {

        module("Dialog Button", {
        });


        test("Submit Button is set to a primary button", function() {
            var button = dialogButton.submit();
            ok(button.$el.hasClass("aui-button-primary"));
        });

        test("Submit Button has text set to submit", function() {
            var button = dialogButton.submit();
            equal(button.$el.text(), "Submit");
        });

        test("Submit Button done callback is executed on click", function() {
            var spy = sinon.spy();
            var button = dialogButton.submit({
                done: spy
            });
            button.click();
            ok(spy.calledOnce);
        });

        test("Submit Button done callback doesn't execute if custom callback is registered", function() {
            var spy = sinon.spy();
            var customCallback = sinon.spy();

            var button = dialogButton.submit({
                done: spy
            });
            button.click(customCallback);
            ok(spy.notCalled);
            ok(customCallback.notCalled);

            button.$el.trigger('ra.dialog.click');
            ok(customCallback.calledOnce);
            ok(spy.notCalled);
        });


        test("Submit Button can be disabled", function() {
            var button = dialogButton.submit();
            button.setEnabled(false);
            ok(!button.isEnabled());
        });

        test("Cancel Button is set to a link button", function() {
            var button = dialogButton.cancel();
            ok(button.$el.hasClass("aui-button-link"));
        });

        test("Cancel Button has text set to cancel", function() {
            var button = dialogButton.cancel();
            equal(button.$el.text(), "Cancel");
        });

        test("Cancel Button done callback is executed on click", function() {
            var spy = sinon.spy();
            var button = dialogButton.cancel({
                done: spy
            });
            button.click();
            ok(spy.calledOnce);
        });

        test("Cancel Button cannot be disabled", function() {
            var button = dialogButton.cancel();
            button.setEnabled(false);
            ok(button.isEnabled());
        });

        test("setEnabled(true) enables a button", function() {
            var button = dialogButton.submit();
            button.$el.attr('aria-disabled', "true");
            button.setEnabled(true);
            ok(button.isEnabled());
        });

        test("setEnabled(false) disables a button", function() {
            var button = dialogButton.submit();
            button.setEnabled(false);
            ok(!button.isEnabled());
        });

        test("Buttons are enabled by default", function() {
            var button = dialogButton.submit();
            ok(button.isEnabled());
        });

        test("click binds an event to ra.dialog.click if passed a function", function() {
            var spy = sinon.spy();
            var button = dialogButton.submit();
            button.click(spy);
            ok(spy.notCalled);
            button.$el.trigger('ra.dialog.click');
            ok(spy.calledOnce);
        });

        test("setText changes the button text", function() {
            var button = dialogButton.submit();
            button.setText('abc123');
            equal(button.$el.text(), 'abc123');
        });

    });
});
