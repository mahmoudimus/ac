_AP.define("jira/workflow-post-function", ["_dollar"], function($) {

    var done = false;

    function postFunctionConfigInput(postFunctionId, val){
        var element = $("#postFunction-config-" + postFunctionId);
        if (val) {
            element.val(val);
        }
        return element.val();
    }

    return {
        isOnWorkflowPostFunctionPage: function(){
            return ($("input[name='postFunction.id']").length > 0);
        },
        registerSubmissionButton: function(rpc, postFunctionId, repeat){
            $(document).delegate("#add_submit, #update_submit", "click", function (e) {
                if (!done || repeat) {
                    e.preventDefault();
                    rpc.setWorkflowConfigurationMessage(function (either) {
                        if (either.valid) {
                            postFunctionConfigInput(postFunctionId, either.value);
                            done = true;
                            $(e.target).click();
                        }
                    });
                }
            });
        },
        getWorkflowConfiguration: function (postFunctionId, callback){
            var val = postFunctionConfigInput(postFunctionId);
            if (callback) {
                callback(val);
            }
            return val;
        }
    };
});
