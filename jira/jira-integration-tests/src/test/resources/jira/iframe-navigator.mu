<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
        <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
    </head>
    <body>

  <p id="navigator-type"></p>
  <p id="navigator-go"></p>

    <script type="text/javascript">
        AP.require(["_dollar", "navigator"],
        function($, n){
            $("#navigator-type")[0].innerHTML = typeof n;
            $("#navigator-go")[0].innerHTML = typeof n.go;
        });
    </script>
    </body>
</html>
