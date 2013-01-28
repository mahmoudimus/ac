(function ($, RA) {

    RA.init();
    RA.getWorkflowConfiguration(function(configuration) {
        $("#configuration").val(configuration);
    });
    RA.WorkflowConfiguration.onSaveValidation(function(e) {
        var conf = $("#configuration").val();
        if (conf == "") {
            return false;
        } else {
            return true;
        }
    });
    RA.WorkflowConfiguration.onSave(function() {
        return $("#configuration").val();
    });

}(jQuery, RA));