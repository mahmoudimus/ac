AP.define("jira", ["_dollar", "_rpc"], function ($, rpc) {

    "use strict";

    /**
    * Get the workflow unique id
    * @memberOf WorkflowConfiguration
    */
    function getUuid() {
        return decodeURI(RegExp('remoteWorkflowPostFunctionUUID=([0-9a-z\-]+)').exec(document.location)[1]);
    }

    var workflowListener,
        validationListener;

    /**
    * @class WorkflowConfiguration
    */
    var WorkflowConfiguration = {
        getUuid: getUuid,
        /**
        * Validate a workflow configuration before saving
        * @memberOf WorkflowConfiguration
        * @param {Function} listener called on validation. Return true / false depending on the result of validation.
        */
        onSaveValidation: function (listener) {
            validationListener = listener;
        },
        /**
        * Attach a callback function to run before a workflow is saved
        * @memberOf WorkflowConfiguration
        * @param {Function} listener called on save.
        */
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

            /**
            * Interact with jira workflows
            * @exports jira
            */
            apis: {
                getUuid: getUuid,
                /**
                * get a workflow configuration object
                *
                * @param {WorkflowConfiguration} callback - the callback that handles the response
                */
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
