(function(){
    define(["iframe/host/_status_helper", "iframe/host/_dollar"], function() {
        _AP.require(["host/_status_helper"], function(statusHelper){
            module("Content Utilities", {

                setup: function() {
                    this.container = $("<div />").attr("id", "qunit-container").appendTo("body");
                },
                teardown: function() {
                    this.container.remove();
                }
            });


            test("createStatusMessages creates a loading status", function() {
                var dom = statusHelper.createStatusMessages();
                equal(dom.find('.ap-loading').length, 1);
            });

            test("createStatusMessages creates a timeout status", function() {
                var dom = statusHelper.createStatusMessages();
                equal(dom.find('.ap-load-timeout').length, 1);
            });

            test("createStatusMessages creates a error status", function() {
                var dom = statusHelper.createStatusMessages();
                equal(dom.find('.ap-load-error').length, 1);
            });

            asyncTest("showLoadingStatus shows the loading status after specified delay", function() {
                var dom = statusHelper.createStatusMessages();
                statusHelper.showLoadingStatus(dom, 100);
                ok(dom.find('.ap-loading').hasClass('hidden'));
                setTimeout(function() {
                    ok(!dom.find('.ap-loading').hasClass('hidden'));
                    start();
                }, 150);
            });

            test("showLoadingStatus shows the loading status immediately when required", function() {
                var dom = statusHelper.createStatusMessages();
                statusHelper.showLoadingStatus(dom);
                ok(!dom.find('.ap-loading').hasClass('hidden'));
            });

            test("showloadTimeoutStatus shows the loading timeout status", function() {
                var dom = statusHelper.createStatusMessages(this.container);
                statusHelper.showloadTimeoutStatus(dom);
                ok(!dom.find('.ap-load-timeout').hasClass('hidden'));

            });

            test("showLoadErrorStatus shows the loading error status", function() {
                var dom = statusHelper.createStatusMessages();
                statusHelper.showLoadErrorStatus(dom);
                ok(!dom.find('.ap-load-error').hasClass('hidden'));

            });

            test("showLoadedStatus hides the status bar", function() {
                var dom = statusHelper.createStatusMessages();
                statusHelper.showLoadedStatus(dom);
                ok(!dom.find('.ap-status:not(.hidden)').length);
            });

        });

    });

})();
