define(['dialog/simple'], function() {

    _AP.require(["dialog/simple"], function(simpleDialog) {

        module("Simple Dialog", {
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
                $(".aui-dialog2").remove();
                dialog.close();
                // clean up mock
                _AP.AJS = null;
            }
        });

        test("dialog options.id sets the dialog id", function() {
            var dialogId = "abc123";
            simpleDialog.create({
                id: dialogId
            });

            equal($("#" + dialogId).length(), 1);
        });

        test("dialog options.w sets the dialog width", function(){
            var dialogId = "foobar";
            simpleDialog.create({
                id: dialogId,
                w: 345,
                chrome: false
            });

            equal($("#" + dialogId).width(), 345);
        });

        test("dialog options.h sets the dialog height", function(){
            var dialogId = "batman";
            simpleDialog.create({
                id: dialogId,
                h: 315,
                chrome: false
            });

            equal($("#" + dialogId).height(), 315);
        });

        test("dialog options.size sets the size of the dialog", function(){
            simpleDialog.create({
                id: "my-dialog",
                size: 'large',
                chrome: false
            });

            ok($("#my-dialog").is(".aui-dialog2-large"), "Size argument was passed to dialog");
        });

        test("dialog options.titleText sets the dialog title", function(){
            var text = "my title text";
            simpleDialog.create({
                id: "my-dialog",
                titleText: text,
                chrome: true
            });

            equal($("#my-dialog h1").text(), text);
        });


        test("Dialog create takes a titleId argument", function() {
            dialog.create({
                id: "my-dialog",
                titleId: "my-title-id",
                chrome: true
            });

            equal($("#my-dialog").attr("aria-labelledby"), "my-title-id", "TitleId attribute was passed to dialog");
        });

        test("Dialog close", function(){
            dialog.create({
                id: "my-dialog",
                chrome: true
            });
            dialog.close();

            ok(this.dialogSpy.hide.calledOnce, "Dialog close was called");
        });

        test("chromeless opens a chromeless dialog", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: false
            });

            equal($(".aui-dialog2-header").length(), 0);
        });

        test("by default, dialogs are chromeless", function(){
            simpleDialog.create({
                id: "my-dialog"
            });

            equal($(".aui-dialog2-header").length(), 0);
        });

        test("options.chrome opens a dialog with chrome", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: false
            });

            equal($(".aui-dialog2-header").length(), 1);
        });

        test("dialogs with chrome contain a submit button", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true
            });

            equal($(".ap-dialog-submit").length(), 1);
        });

        test("dialogs with chrome contain a cancel button", function(){
            simpleDialog.create({
                id: "my-dialog",
                chrome: true
            });

            equal($(".ap-dialog-cancel").length(), 1);
        });

    });
});
