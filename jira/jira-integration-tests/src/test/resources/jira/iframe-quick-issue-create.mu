<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>

  <button id="dialog">Click me to open issue create</button>
  <div id="summarytext"></div>
    <script type="text/javascript">
        AP.require(['jira', '_dollar'], function(j, $){
            $("#dialog").bind("click", function(){
                var func = function (issues) {
                    $("#summarytext")[0].innerHTML = issues[0]['fields']['summary'];
                };

                j.openCreateIssueDialog(func, {
                    issueType: 1
                });
            });
        });
    </script>
  </body>
</html>
