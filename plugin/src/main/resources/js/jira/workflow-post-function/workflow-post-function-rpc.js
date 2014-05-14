_AP.define("jira/workflow-post-function/rpc", ["_dollar","_rpc", "jira/workflow-post-function"], function($, rpc, workflowPostFunction) {
    rpc.extend(function(){
        if(!workflowPostFunction.isOnWorkflowPostFunctionPage()){
            return {};
        }
        return {
            init: function (state, xdm) {
                var callback = state.setWorkflowConfigurationMessage;
                workflowPostFunction.registerSubmissionButton(state.productContext["postFunction.id"], callback);
            },
            internals: {
                getWorkflowConfiguration: function (callback) {
                    var val = workflowPostFunction.postFunctionConfigInput(this.productContext["postFunction.id"]);
                    if (callback) {
                        callback(val);
                    }
                    return val;
                }
            },
            stubs: ["setWorkflowConfigurationMessage"]
        };
    });
});
