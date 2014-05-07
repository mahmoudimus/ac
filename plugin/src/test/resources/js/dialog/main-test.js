define(['dialog/main'], function() {

    _AP.require(["dialog/main"], function(simpleDialog) {

        module("Main Dialog", {
            setup: function(){
                this.dialogSpy = {
                    show: sinon.spy(),
                    on: sinon.spy(),
                    remove: sinon.spy(),
                    hide: sinon.spy()
                };
                this.layerSpy = {
                    changeSize: sinon.spy()
                };

                AJS.dialog2 = sinon.stub().returns(this.dialogSpy);
                AJS.layer = sinon.stub().returns(this.layerSpy);

            },
            teardown: function(){
                // clean up mock
                _AP.AJS = null;
                AJS.dialog2 = null;
                AJS.layer = null;
            }
        });

        test("dialog options.id sets the dialog id", function() {
            var dialogId = "abc123";
            var dialog = simpleDialog.create({
                id: dialogId
            });

            equal(AJS.dialog2.args[0][0].attr('id'), dialogId);
        });

        test("dialog options.width sets the dialog width", function(){
            var dialogId = "foobar";
            simpleDialog.create({
                id: dialogId,
                width: 345,
                chrome: false
            });

            equal(this.layerSpy.changeSize.args[0][0], 345);
        });

        test("dialog options.height sets the dialog height", function(){
            var dialogId = "batman";
            simpleDialog.create({
                id: dialogId,
                height: 315,
                chrome: false
            });

            equal(this.layerSpy.changeSize.args[0][1], 315);
        });

        test("dialog options.size sets the size of the dialog", function(){
            simpleDialog.create({
                id: "my-dialog",
                size: 'large',
                chrome: false
            });

            ok(AJS.dialog2.args[0][0].is(".aui-dialog2-large"), "Size argument was passed to dialog");
        });

        test("dialog options.header sets the dialog title", function(){
            var text = "my title text";
            simpleDialog.create({
                id: "my-dialog",
                header: text,
                chrome: true
            });

            equal(AJS.dialog2.args[0][0].find('h1').text(), text);
        });


        test("Dialog create takes a titleId argument", function() {
            simpleDialog.create({
                id: "my-dialog",
                titleId: "my-dialog",
                chrome: true
            });
            // aui appends "dialog-title" to the end of your dialog titles.
            equal(AJS.dialog2.args[0][0].attr("aria-labelledby"), "my-dialog-dialog-title", "TitleId attribute was passed to dialog");
        });

        test("Dialog close", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true
            });
            simpleDialog.close();

            ok(this.dialogSpy.hide.calledOnce, "Dialog close was called");
        });

        test("chromeless opens a chromeless dialog", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: false
            });

            equal(AJS.dialog2.args[0][0].find(".aui-dialog2-header").length, 0);
        });

        test("by default, dialogs are chromeless", function(){
            simpleDialog.create({
                id: "my-dialog"
            });

            equal(AJS.dialog2.args[0][0].find(".aui-dialog2-header").length, 0);
        });

        test("options.chrome opens a dialog with chrome", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true
            });

            equal(AJS.dialog2.args[0][0].find(".aui-dialog2-header").length, 1);
        });

        test("dialogs with chrome contain a submit button", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true
            });

            equal(AJS.dialog2.args[0][0].find(".ap-dialog-submit").length, 1);
        });

        test("dialogs with chrome contain a cancel button", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true
            });

            equal(AJS.dialog2.args[0][0].find(".ap-dialog-cancel").length, 1);
        });

        test("dialog with a submit button can set the text", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true,
                submitText: "some submit text"
            });
            equal(AJS.dialog2.args[0][0].find(".ap-dialog-submit").text(), "some submit text");
        });

        test("dialog with a cancel button can set the text", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true,
                cancelText: "some cancel text"
            });
            equal(AJS.dialog2.args[0][0].find(".ap-dialog-cancel").text(), "some cancel text");
        });


    });
});
