<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/remotable-plugins/all.css">
    <script src="{{baseurl}}/remotable-plugins/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <h1>Admin</h1>
    Message:<div id="message">Success</div>
    User:<div id="user"></div>
    User ID:<div id="userId"></div>
    <script>
        (function (AP) {
          // general api testing
          AP.getUser(function(user) {
            document.getElementById("user").innerHTML = user.fullName;
            document.getElementById("userId").innerHTML = user.id;
          });
        })(AP);
    </script>
  </body>
</html>


