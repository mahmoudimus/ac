<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div>
      <button class="aui-button" id="dialog-open-button">Open dialog</button>
    </div>
    <script>
      AP.require(["_dollar", "dialog"], function($, dialog) {
        $("#dialog-open-button").bind("click", function() {
          dialog.create({
            url: "/dialog"
          });
        });
      });
    </script>
  </body>
</html>
