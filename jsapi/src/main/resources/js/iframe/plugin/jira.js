(function(){
  "use strict";
  var workflowListener,
      validationListener,
      dashboardItemEditListener = function(){},
      issueCreateListener;

  function isFunction(arg){
    return (typeof arg === "function");
  }

  /**
   * @class WorkflowConfiguration
   */
  var WorkflowConfiguration = {
    /**
    * Retrieves a workflow configuration object.
    *
    * @param {WorkflowConfiguration} callback - the callback that handles the response.
    */

    /**
     * Validate a workflow configuration before saving
     * @noDemo
     * @memberOf WorkflowConfiguration
     * @param {Function} listener called on validation. Return false to indicate that validation has not passed and the workflow cannot be saved.
     */
    onSaveValidation: function (listener) {
      validationListener = listener;
    },
    /**
     * Attach a callback function to run when a workflow is saved
     * @noDemo
     * @memberOf WorkflowConfiguration
     * @param {Function} listener called on save.
     */
    onSave: function (listener) {
        workflowListener = listener;
    },
    /**
     * Save a workflow configuration if valid.
     * @noDemo
     * @memberOf WorkflowConfiguration
     * @returns {WorkflowConfigurationTriggerResponse} An object Containing `{valid, value}` properties.valid (the result of the validation listener) and value (result of onSave listener) properties.
     */
    trigger: function () {
      var valid = true;
      if (isFunction(validationListener)) {
        valid = validationListener.call();
      }
      /**
       * An object returned when the {@link WorkflowConfiguration} trigger method is invoked.
       * @name WorkflowConfigurationTriggerResponse
       * @class
       * @property {Boolean} valid The result of the validation listener {@link WorkflowConfiguration.onSaveValidation}
       * @property {*} value The result of the {@link WorkflowConfiguration.onSave}
       */
      var workflowListenerValue;
      if(workflowListener){
        workflowListenerValue = workflowListener.call();
      }
      var response = {
        valid: valid,
        value: valid ? "" + workflowListenerValue : undefined
      };
      AP.jira._submitWorkflowConfigurationResponse(response);
      return response;
    }
  };

  AP.register({
    jira_workflow_post_function_submit: function() {
      WorkflowConfiguration.trigger();
    }
  });

  AP.jira.WorkflowConfiguration = AP._hostModules.jira.WorkflowConfiguration = WorkflowConfiguration;


  AP.register({
    jira_dashboard_item_edit: function() {
      dashboardItemEditListener.call();
    }
  });
  
  function onDashboardItemEdit(listener){
    dashboardItemEditListener = listener;
  }

  AP.jira.DashboardItem = AP._hostModules.jira.DashboardItem = {onDashboardItemEdit: onDashboardItemEdit};



}());