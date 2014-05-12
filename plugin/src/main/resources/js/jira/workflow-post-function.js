_AP.define("jira/workflow-post-function", ["_dollar","_rpc"], function($, rpc) {

    function postFunctionConfigInput(postFunctionId, val){
        var element = $("#postFunction-config-" + postFunctionId);
        if (val) {
            element.val(val);
        }
        return element.val();
    }

    function isOnWorkflowPostFunctionPage () {
        return ($("input[name='postFunction.id']").length > 0);
    }

    rpc.extend(function(){
        return {
            init: function (state, xdm) {
                if(!isOnWorkflowPostFunctionPage()){
                    return;
                }
                var done = false;
                $(document).delegate("#add_submit, #update_submit", "click", function (e) {
                    if(!done){
                        e.preventDefault();
                        state.setWorkflowConfigurationMessage(function (either) {
                            if (either.valid) {
                                postFunctionConfigInput(xdm.productContext["postFunction.id"], either.value);
                                done = true;
                                $(e.target).click();
                            }
                        });
                    }
                });

            },
            internals: {
                getWorkflowConfiguration: function (callback) {
                    var val = postFunctionConfigInput(this.productContext["postFunction.id"]);
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
