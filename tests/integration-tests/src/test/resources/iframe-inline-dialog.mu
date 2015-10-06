<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div class="ac-content">
      <button class="aui-button" id="inline-dialog-hide-button">Hide</button>
    </div>
    <script type="text/javascript">
      AP.require(["_dollar", "inline-dialog"], function($, inlineDialog) {
        $("#inline-dialog-hide-button").bind("click", function() {
          inlineDialog.hide();
        });
      });
    </script>
  </body>
</html>
