<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseUrl}}/remotable-plugins/all.css">
    <script src="{{baseUrl}}/remotable-plugins/all-debug.js" type="text/javascript"></script>
    <script src="public/jquery-1.7.min.js" type="text/javascript"></script>
  </head>
  <body>
    <div>
      Current user: <span id="user"></span>
    </div>
    <div>
      Was Submitted: <span id="submitted">false</span>
    </div>
    <script type="text/javascript">
      AP.getUser(function(result) {
        $("#user").text(result.fullName);
      });
      AP.Dialog.onDialogMessage("submit", function() {
        var $submitted = $("#submitted");
        var isFalse = $submitted.text() === "false";
        if (isFalse) $submitted.text("true");
        return !isFalse;
      });
    </script>
  </body>
</html>
