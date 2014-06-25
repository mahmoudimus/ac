<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <p id="macro-body"></p>
    <script type="text/javascript">

        function getMacroBodyParagraph() {
            return document.getElementById("macro-body");
        }

        AP.require(["confluence", "dialog"], function (confluence, dialog) {

          confluence.getMacroBody(function (data) {
            getMacroBodyParagraph().textContent = data;
          });

          function onSubmit() {
            confluence.saveMacro({}, "cat pictures and more");
            confluence.closeMacroEditor();
            return true;
          }

          dialog.getButton("submit").bind(onSubmit);
        });

    </script>
  </body>
</html>
