define("ac/jira/workflow-post-function", ["ac/jira/workflow-post-function-utils"], function(workflowUtils){
  "use strict";
  var workflowSubmissionCallback = function(){}; //noop

  connectHost.onIframeEstablished(function(){
    if(workflowUtils.isOnWorkflowPostFunctionPage()){
      var postFunctionId = workflowUtils.getPostFunctionId();
      if(typeof postFunctionId === "string"){
        workflowUtils.registerSubmissionButton(postFunctionId, function(callback){
          workflowSubmissionCallback = callback;
          connectHost.broadcastEvent("jira_workflow_post_function_submit", {});
        });
      }
    }
  });

  return {
    getWorkflowConfiguration: function(callback){
      if(!workflowUtils.isOnWorkflowPostFunctionPage()){
        return;
      }
      var val = workflowUtils.postFunctionConfigInput(callback._context.extension.options.productContext["postFunction.id"]);
      if (callback) {
       callback(val);
      }
      return val;
    },
    _submitWorkflowConfigurationResponse: function(data, callback){
      workflowSubmissionCallback(data);
    }
  };
});