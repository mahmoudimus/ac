var xdmMockJira;
(function(){
    var context = require.config({
        context: Math.floor(Math.random() * 1000000),
        baseUrl: 'base/src/main/resources/js/iframe/plugin',
        map: {
            '*': {
                '_xdm': '_xdmMockJiraTest'
            }
        },
        paths: {
            '_xdmMockJiraTest': '/base/src/test/resources/js/iframe/plugin/_xdmMockJiraTest'
        }
    });

    xdmMockJira = {
        init: function() {},
        getWorkflowConfiguration: sinon.spy()
    };

    context(["_rpc", "jira"], function(_rpc, jira) {
        _rpc.init();

        module("Jira plugin", {
            setup: function(){
                xdmMockJira.getWorkflowConfiguration.reset();
            }
        });

        test('getWorkflowConfiguration calls remote getWorkflowConfiguration', function(){
            jira.getWorkflowConfiguration();
            ok(xdmMockJira.getWorkflowConfiguration.calledOnce);
        });

        test('getWorkflowConfiguration passes callback to remote getWorkflowConfiguration', function(){
            var callback = sinon.spy();

            jira.getWorkflowConfiguration(callback);

            deepEqual(xdmMockJira.getWorkflowConfiguration.args[0][0], callback);
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


    });

})();
