<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/remotable-plugins/all.css">
    <script src="{{baseurl}}/remotable-plugins/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div>
      Current user: <span id="user"></span>
    </div>
    <div>
      Was Submitted: <span id="submitted">false</span>
    </div>
    <script type="text/javascript">
    (function (AP) {
      AP.getUser(function(result) {
        document.getElementById("user").innerHTML = result.fullName;
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
