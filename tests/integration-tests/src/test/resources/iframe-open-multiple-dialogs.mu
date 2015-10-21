<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div>
      <button class="aui-button" id="dialog-open-button-for-multiple-dialogs">Open dialog that opens another dialog</button>
      <br>
      Dialog Close Data: <span id="dialog-close-data"></span>
    </div>
    <script>

      // test opening a dialog
      AP.require(["_dollar", "dialog"], function($, dialog) {
        $("#dialog-open-button-for-multiple-dialogs").bind("click", function() {
          dialog.create({
                width: "400px",
                height: "300px",
                key: "my-webitem-dialog",
                chrome: "true"
          }).on("close", function (data) {
            $("#dialog-close-data")[0].innerHTML = data;
          });

          dialog.create({
              key: "full-page-dialog",
              width: "800",
              height: "400"
          });

        });
      });
    </script>
  </body>
</html>
