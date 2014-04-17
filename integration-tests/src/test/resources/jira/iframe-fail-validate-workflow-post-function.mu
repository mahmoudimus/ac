<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <script type="text/javascript">
        AP.require(["jira"], function(jira){

            jira.WorkflowConfiguration.onSaveValidation(function() {
                return false;
            });

            jira.WorkflowConfiguration.onSave(function() {
                return "workflow configuration text";
            });
        });
    </script>
  </body>
</html>
