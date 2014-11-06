define("jira/workflow-post-function", ["_dollar"], function ($) {

    function postFunctionConfigInput (postFunctionId, val) {
        var element = $("#postFunction-config-" + postFunctionId);
        if (val) {
            element.val(val);
        }
        return element.val();
    }

    function isOnWorkflowPostFunctionPage () {
        return ($("input[name='postFunction.id']").length > 0);
    }

    function registerSubmissionButton (postFunctionId, callback, repeat) {
        if(!isOnWorkflowPostFunctionPage()){
            throw "Not on a workflow configuration page";
        }
        var done = false;
        $(document).delegate("#add_submit, #update_submit", "click", function (e) {
            if(!done || repeat){
                e.preventDefault();
                callback(function (either) {
                    if (either.valid) {
                        postFunctionConfigInput(postFunctionId, either.value);
                        done = true;
                        $(e.target).click();
                    }
                });
            }
        });
    }

    return {
        postFunctionConfigInput: postFunctionConfigInput,
        isOnWorkflowPostFunctionPage: isOnWorkflowPostFunctionPage,
        registerSubmissionButton: registerSubmissionButton
    };
});
