(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin'
    });

    window.xdmMock = {
        init: function() {},
        getWorkflowConfiguration: sinon.spy(),
        triggerJiraEvent: sinon.spy(),
        openDatePicker: sinon.spy()
    };


    context(["jira"], function() {
        AP.require(['jira'],function(jira){

            xdmMock.getWorkflowConfiguration = sinon.spy();
            xdmMock.triggerJiraEvent = sinon.spy();
            xdmMock.openDatePicker = sinon.spy();

            module("Jira plugin", {
                setup: function(){
                    xdmMock.getWorkflowConfiguration.reset();
                    xdmMock.triggerJiraEvent.reset();
                    xdmMock.openDatePicker.reset();
                }
            });

            test('getWorkflowConfiguration calls remote getWorkflowConfiguration', function(){
                jira.getWorkflowConfiguration();
                ok(xdmMock.getWorkflowConfiguration.calledOnce);
            });

            test('getWorkflowConfiguration passes callback to remote getWorkflowConfiguration', function(){
                var callback = sinon.spy();

                jira.getWorkflowConfiguration(callback);

                deepEqual(xdmMock.getWorkflowConfiguration.args[0][0], callback);
            });

            test('onSave callback is triggered when event is triggered', function(){
                var callback = sinon.spy();

                jira.WorkflowConfiguration.onSaveValidation(sinon.stub().returns(true));
                jira.WorkflowConfiguration.onSave(callback);
                jira.WorkflowConfiguration.trigger();

                ok(callback.calledOnce);
            });

            test('when triggered onSave value is returned as value', function(){
                var value = 'abc123';

                jira.WorkflowConfiguration.onSaveValidation(sinon.stub().returns(true));
                jira.WorkflowConfiguration.onSave(sinon.stub().returns(value));

                equal(jira.WorkflowConfiguration.trigger().value, value);
            });

            test('valid is true when workflow validation function returns true', function(){
                jira.WorkflowConfiguration.onSaveValidation(sinon.stub().returns(true));

                ok(jira.WorkflowConfiguration.trigger().valid);
            });

            test('valid is false when workflow validation function returns false', function(){
                jira.WorkflowConfiguration.onSaveValidation(sinon.stub().returns(false));

                ok(!jira.WorkflowConfiguration.trigger().valid);
            });

            test('refreshIssuePage calls remote triggerJiraEvent', function(){
                jira.refreshIssuePage();
                ok(xdmMock.triggerJiraEvent.calledOnce);
            });

            test('refreshIssuePage calls triggerJiraEvent with correct event name', function(){
                jira.refreshIssuePage();
                equal(xdmMock.triggerJiraEvent.args[0][0], 'refreshIssuePage');
            });

            test('openDatePicker opens if only position is provided', function(){
                var onSelected = sinon.stub();

                jira.openDatePicker({
                    position: {
                        top: 0,
                        left: 0
                    },
                    date: "2011-12-13T15:20+01:00",
                    onSelect: onSelected
                });

                ok(xdmMock.openDatePicker.calledOnce);
            });

            test('openDatePicker uses position of the element if both options.position and options.element are provided', function(){
                var onSelected = sinon.stub();
                var mockDiv = {
                    nodeType: 1,
                    getBoundingClientRect: function() {
                        return {
                            top: 900,
                            left: 1000,
                            height: 100
                        }
                    }
                };

                jira.openDatePicker({
                    element: mockDiv,
                    position: {
                        top: 123,
                        left: 456
                    },
                    date: "2011-12-13T15:20+01:00",
                    onSelect: onSelected
                });

                ok(xdmMock.openDatePicker.calledOnce);

                var firstCallOptions = xdmMock.openDatePicker.args[0][0];
                deepEqual(
                    firstCallOptions.position,
                    {
                        top: 1000,
                        left: 1000
                    }
                );
            });

            test('openDatePicker opens if only element is provided', function(){
                var div = document.createElement("div");
                var onSelected = sinon.stub();

                jira.openDatePicker({
                    element: div,
                    date: "2011-12-13T15:20+01:00",
                    onSelect: onSelected
                });

                ok(xdmMock.openDatePicker.calledOnce);
            });


            test('openDatePicker throws exception when neither element nor position is provided', function(){
                var onSelected = sinon.stub();

                throws(function() {
                    jira.openDatePicker({
                        date: "2011-12-13T15:20+01:00",
                        onSelect: onSelected
                    });
                }, new Error("Providing either options.position or options.element is required."));

                ok(xdmMock.openDatePicker.notCalled);
            });

            test('openDatePicker throws exception onSelected callback is not provided', function(){
                var div = document.createElement("div");

                throws(function() {
                    jira.openDatePicker({
                        element: div,
                        date: "2011-12-13T15:20+01:00"
                    });
                }, new Error("options.onSelect function is a required parameter."));

                ok(xdmMock.openDatePicker.notCalled);
            });

        });
    });

})();
