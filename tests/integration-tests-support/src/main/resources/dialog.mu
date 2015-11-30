<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div>
      Location: <span id="location"></span>
    </div>
    <div>
      Was Submitted: <span id="submitted">false</span>
    </div>
    <script type="text/javascript">
    (function (AP) {
      AP.getLocation(function(location) {
        document.getElementById("location").innerHTML = location;
      });
      AP.Dialog.onDialogMessage("submit", function() {
        var $submitted = document.getElementById("submitted");
        var isFalse = $submitted.innerHTML === "false";
        if (isFalse) $submitted.innerHTML = "true";
        return !isFalse;
      });
      })(AP);
    </script>
  </body>
</html>
