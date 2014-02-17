<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <p id="description">Select from:</p>
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
          var selectedValue = footy.options[footy.selectedIndex].value;
          confluence.saveMacro({
            footy: selectedValue
          });
          return true;
        });
      });
    </script>
  </body>
</html>
