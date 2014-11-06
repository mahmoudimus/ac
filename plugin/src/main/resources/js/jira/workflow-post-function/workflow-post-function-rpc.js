require(["_dollar","_rpc", "jira/workflow-post-function"], function($, rpc, workflowPostFunction) {
    "use strict";

    rpc.extend(function () {
        return {
            init: function (state, xdm) {
                if(!workflowPostFunction.isOnWorkflowPostFunctionPage()){
                    return;
                }
                var callback = xdm.setWorkflowConfigurationMessage;
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
