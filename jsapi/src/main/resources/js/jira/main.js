require(['ac/jira/events','ac/jira/workflow-post-function'], function(jiraEvents, workflowPostFunctions){
  connectHost.defineModule("jira", {
    refreshIssuePage: jiraEvents.refreshIssuePage,
    getWorkflowConfiguration: workflowPostFunctions.getWorkflowConfiguration,
    _submitWorkflowConfigurationResponse: workflowPostFunctions._submitWorkflowConfigurationResponse
    // isDashboardItemEditable: workflowPostFunctions.isDashboardItemEditable,
    // openCreateIssueDialog: workflowPostFunctions.openCreateIssueDialog,
    // setDashboardItemTitle: workflowPostFunctions.setDashboardItemTitle
  });
});
