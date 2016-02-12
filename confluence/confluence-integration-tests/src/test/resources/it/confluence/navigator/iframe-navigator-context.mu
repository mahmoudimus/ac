<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">

          <script src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
          <script src="//aui-cdn.atlassian.com/aui-adg/5.8.12/js/aui-soy.js" type="text/javascript"></script>
          <script src="//aui-cdn.atlassian.com/aui-adg/5.8.12/js/aui.js" type="text/javascript"></script>
          <script src="//aui-cdn.atlassian.com/aui-adg/5.8.12/js/aui-datepicker.js"></script>

        <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
    </head>
    <body>
        <div id="ac-target"></div>
        <div id="ac-contentId"></div>
        <div id="ac-contentType"></div>
        <script type="text/javascript">
            var showLocation = function(location) {
                $('#ac-target').text(location.target);
                $('#ac-contentId').text(location.context.contentId);
                $('#ac-contentType').text(location.context.contentType);
            };

            AP.require('navigator', function(navigator) {
                navigator.getLocation(showLocation);
            });
        </script>
    </body>
</html>
