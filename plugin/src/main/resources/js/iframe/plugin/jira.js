AP.define("jira", ["_dollar", "_rpc"], function ($, rpc) {

    "use strict";

    /**
    * Get the workflow unique id
    * @memberOf WorkflowConfiguration
    */
    function getUuid() {
        return decodeURI(RegExp('postFunction\\.id=([0-9a-z\\-]+)').exec(document.location)[1]);
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
        * @param {Function} listener called on validation. Return false to indicate that validation has not passed and the workflow cannot be saved.
        */
        onSaveValidation: function (listener) {
            validationListener = listener;
        },
        /**
        * Attach a callback function to run when a workflow is saved
        * @memberOf WorkflowConfiguration
        * @param {Function} listener called on save.
        */
        onSave: function (listener) {
            workflowListener = listener;
        },
        /**
        * Save a workflow configuration if valid.
        * @memberOf WorkflowConfiguration
        * @returns {WorkflowConfigurationTriggerResponse} An object Containing `{valid, uuid, value}` properties.valid (the result of the validation listener), uuid and value (result of onSave listener) properties.
        */
        trigger: function () {
            var valid = validationListener.call(), undef;
            /**
            * An object returned when the {@link WorkflowConfiguration} trigger method is invoked.
            * @name WorkflowConfigurationTriggerResponse
            * @class
            * @property {Boolean} valid The result of the validation listener {@link WorkflowConfiguration.onSaveValidation}
            * @property {String} uuid uuid of the {@link WorkflowConfiguration}
            * @property {*} value The result of the {@link WorkflowConfiguration.onSave}
            */
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
            * Allows custom validation and save callback functions for jira workflow configurations.
            * @see {WorkflowConfiguration}
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
