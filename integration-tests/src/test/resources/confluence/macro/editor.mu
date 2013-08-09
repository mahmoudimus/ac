<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseUrl}}/atlassian-connect/all.css">
    <script src="{{baseUrl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <select id="footy">
      <option name="American Football">American Football</option>
      <option name="Soccer">Soccer</option>
      <option name="Rugby Union">Rugby Union</option>
      <option name="Rugby League">Rugby League</option>
    </select>
    <script type="text/javascript">
      AP.require(["confluence", "dialog"], function (confluence, dialog) {
        dialog.getButton("submit").bind(function() {
          var footy = document.getElementById("footy");
          confluence.saveMacro({
            footy: footy.val()
          });
          return true;
        });
      });
    </script>
  </body>
</html>
