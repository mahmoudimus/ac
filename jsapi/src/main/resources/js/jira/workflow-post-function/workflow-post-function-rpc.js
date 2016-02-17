(function(connectHost, require){
  "use strict";
  var workflowSubmissionCallback = function(){}; //noop

  connectHost.onIframeEstablished(function(){
    require(["ac/jira/workflow-post-function"], function(workflowPostFunction){
      if(workflowPostFunction.isOnWorkflowPostFunctionPage()){
        var postFunctionId = workflowPostFunction.getPostFunctionId();
        if(typeof postFunctionId === "string"){
          workflowPostFunction.registerSubmissionButton(postFunctionId, function(callback){
            workflowSubmissionCallback = callback;
            connectHost.broadcastEvent("jira_workflow_post_function_submit", {});
          });
        }
      }
    });
  });


  connectHost.defineModule("jira", {
    getWorkflowConfiguration: function(callback){
      require(["ac/jira/workflow-post-function"], function(workflowPostFunction){
        if(!workflowPostFunction.isOnWorkflowPostFunctionPage()){
          return;
        }
        var val = workflowPostFunction.postFunctionConfigInput(callback._context.extension.options.productContext["postFunction.id"]);
        if (callback) {
          callback(val);
        }
        return val;
      });
    },
    _submitWorkflowConfigurationResponse: function(data, callback){
      workflowSubmissionCallback(data);
    },
    isDashboardItemEditable: function(callback){

    },
    openCreateIssueDialog: function(fields, callback){

    },
    refreshIssuePage: function(callback){

    },
    setDashboardItemTitle: function(title, callback){

    }
  });
}(connectHost, require));