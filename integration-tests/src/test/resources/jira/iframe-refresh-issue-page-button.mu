<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div class="ac-content">
        <button id="refresh-issue-page-button">Refresh</button>
    </div>
    <script type="text/javascript">
        AP.require(["_dollar", "jira"], function($, jira) {
            $("#refresh-issue-page-button").bind("click", function(){
                jira.refreshIssuePage();
            });
        });
    </script>
  </body>
</html>
