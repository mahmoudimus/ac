(function($, require){
    "use strict";
    log('workflow post function included');
    require(["ac/jira/workflow-post-function", 'connect-host'], function(workflowPostFunction, _AP) {
        log('in workflow post function');
        if(_AP){
            log("_AP TRUE");
            if(_AP.extend){
                log("_AP.extend true");
            } else {
                log('no ap extend');
            }
        } else {
            log('no ap');
        }
        _AP.extend(function () {
            log('extend workflow post function');
            return {
                init: function workflowInit(state, xdm) {
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
})(AJS.$, require);
