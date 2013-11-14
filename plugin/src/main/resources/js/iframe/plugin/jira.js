AP.define("jira", ["_dollar", "_rpc"], function ($, rpc) {

    "use strict";

    function getUuid() {
        return decodeURI(RegExp('remoteWorkflowPostFunctionUUID=([0-9a-z\-]+)').exec(document.location)[1]);
    }

    var workflowListener,
        validationListener;

    var WorkflowConfiguration = {
        getUuid: getUuid,
        onSaveValidation: function (listener) {
            validationListener = listener;
        },
        onSave: function (listener) {
            workflowListener = listener;
        },
        trigger: function () {
            var valid = validationListener.call(), undef;
            return {
                valid: valid,
                uuid: valid ? this.getUuid() : undef,
                value: valid ? "" + workflowListener.call() : undef
            };
        }
    };

    var apis = rpc.extend(function (remote) {

        return {

            apis: {
                getUuid: getUuid,
                // get a workflow configuration object
                //
                // @param callback function (workflow) {...}
                getWorkflowConfiguration: function (callback) {
                    remote.getWorkflowConfiguration(this.getUuid(), callback);
                }

            },

            internals: {

                setWorkflowConfigurationMessage: function () {
                    return WorkflowConfiguration.trigger();
                }

            }

        };

    });

    return $.extend(apis, {
        WorkflowConfiguration: WorkflowConfiguration
    });

});
