(function($, extend, require){
    "use strict";
    require(["ac/jira/workflow-post-function"], function(workflowPostFunction) {

        extend(function () {
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
})(AJS.$, _AP.extend, require);
