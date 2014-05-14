(function(){
    define(['jira/workflow-post-function'], function() {

        _AP.require(["jira/workflow-post-function", "_dollar"], function(workflowPostFunction, $) {

            module("JIRA workflow post function", {
                setup: function() {
                    this.uuid = "ABC123";
                    $(document).undelegate("#add_submit, #update_submit");

                    var $content = $('<input id="postFunction-config-' +
                    this.uuid + '"  name="postFunction.config-' + this.uuid + '" value="" type="hidden"/>' +
                    '<input name="postFunction.id" value="' + this.uuid + '" type="hidden" />');

                    $content.append('<input type="submit" id="add_submit" />');

                    this.fixture = $('<div id="qunit-fixture">').append($content).appendTo('body');

                },
                teardown: function() {
                    $(document).undelegate();
                    this.fixture.remove();
                }
            });

            test("isOnWorkflowPostFunctionPage returns true when workflow fields are loaded", function(){
                ok(workflowPostFunction.isOnWorkflowPostFunctionPage());
            });

            test("isOnWorkflowPostFunctionPage returns false without workflow fields", function(){
                this.fixture.remove();
                ok(!workflowPostFunction.isOnWorkflowPostFunctionPage());
            });

            test("registerSubmissionButton triggers the callback on click", function(){
                var spy = sinon.spy();
                workflowPostFunction.registerSubmissionButton(this.uuid, spy);

                ok(!spy.called);

                $("#add_submit").trigger('click');

                ok(spy.calledOnce);
            });

            test("registerSubmissionButton trigger causes the form fields value to update when valid", function(){
                var WORKFLOW_CONFIG_MESSAGE = "workflow config message",
                spy = sinon.spy();

                workflowPostFunction.registerSubmissionButton(this.uuid, spy);
                $("#add_submit").trigger('click');

                //invoke the function passed to the mock.
                spy.args[0][0]({
                    valid: true,
                    value: WORKFLOW_CONFIG_MESSAGE
                }); 

                equal($('#postFunction-config-' + this.uuid).val(), WORKFLOW_CONFIG_MESSAGE);
            });

            test("registerSubmissionButton trigger does not udpate when invalid", function(){
                var WORKFLOW_CONFIG_MESSAGE = "workflow config message",
                spy = sinon.spy();

                workflowPostFunction.registerSubmissionButton(this.uuid, spy, true);
                $("#add_submit").trigger('click');

                //invoke the function passed to the mock.
                spy.args[0][0]({
                    valid: false,
                    value: WORKFLOW_CONFIG_MESSAGE
                }); 

                equal($('#postFunction-config-' + this.uuid).val(), "");
            });

            test("getWorkflowConfiguration returns the workflow configuration value", function(){
                $('#postFunction-config-' + this.uuid).val("some workflow config");
                var workflowconfig = workflowPostFunction.getWorkflowConfiguration(this.uuid);

                equal($('#postFunction-config-' + this.uuid).val(), workflowconfig);
            });


            test("getWorkflowConfiguration invokes the callback if provided", function(){
                var WORKFLOW_VALUE = "some workflow",
                callback = sinon.spy();

                $('#postFunction-config-' + this.uuid).val(WORKFLOW_VALUE);
                workflowPostFunction.getWorkflowConfiguration(this.uuid, callback);

                equal(callback.args[0][0], WORKFLOW_VALUE);
            });

        });
    });
})();
