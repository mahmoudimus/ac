<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div style="height:9000px; width: 100%">
      <form class="aui">
        <input class="text long-field" type="text" id="dialog-close-data" name="dialog-close-data" title="Message" value="test dialog close data">
      </form>
      <br>
      <button class="aui-button" id="dialog-close-button">Close dialog</button>
    </div>
    <script>
      AP.require(["_dollar", "dialog"], function($, dialog) {
        $("#dialog-close-button").bind("click", function() {
          dialog.close($("#dialog-close-data")[0].value);
        });
      });
    </script>
  </body>
</html>
