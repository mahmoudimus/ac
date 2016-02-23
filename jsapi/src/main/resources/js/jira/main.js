define("ac/jira", ['ac/jira/events','ac/jira/workflow-post-function', 'ac/jira/dashboard-item'], function(jiraEvents, workflowPostFunctions, dashboardItem){
  return {
    refreshIssuePage: jiraEvents.refreshIssuePage,
    getWorkflowConfiguration: workflowPostFunctions.getWorkflowConfiguration,
    _submitWorkflowConfigurationResponse: workflowPostFunctions._submitWorkflowConfigurationResponse,
    isDashboardItemEditable: dashboardItem.isDashboardItemEditable,
    // openCreateIssueDialog: workflowPostFunctions.openCreateIssueDialog,
    setDashboardItemTitle: dashboardItem.setDashboardItemTitle
  };
});