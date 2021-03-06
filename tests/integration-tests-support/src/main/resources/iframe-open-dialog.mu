<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div>
      <button class="aui-button" id="dialog-open-button-key">Open dialog via key</button>
      <button class="aui-button" id="dialog-open-button-url">Open dialog via URL</button>
      <br>
      Dialog Close Data: <span id="dialog-close-data"></span>
    </div>
    <script>
      // TODO: remove when XML descriptor support is gone (it tests opening a dialog by arbitrary URL)
      AP.require(["_dollar", "dialog"], function($, dialog) {
        $("#dialog-open-button-url").bind("click", function() {
          dialog.create({
            width: "654px",
            height: "918px",
            url: "/dialog"
          }).on("close", function (data) {
            $("#dialog-close-data")[0].innerHTML = data;
          });
        });
      });
      // test opening a dialog by module key
      AP.require(["_dollar", "dialog"], function($, dialog) {
        $("#dialog-open-button-key").bind("click", function() {
          dialog.create({
                width: "231px",
                height: "356px",
                key: "{{dialogKey}}"
          }).on("close", function (data) {
            $("#dialog-close-data")[0].innerHTML = data;
          });
        });
      });
    </script>
  </body>
</html>
