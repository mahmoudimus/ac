(function ($, AP) {

    AP.getWorkflowConfiguration(function(configuration) {
        $("#configuration").val(configuration);
    });
    AP.WorkflowConfiguration.onSaveValidation(function() {
        return !!$("#configuration").val();
    });
    AP.WorkflowConfiguration.onSave(function() {
        return $("#configuration").val();
    });

}(jQuery, AP));
