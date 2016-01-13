<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
        <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
    </head>
    <body bgcolor="#FF0000">
        <div id="ac-target"></div>
        <div id="ac-contentId"></div>
        <div id="ac-contentType"></div>
        <script type="text/javascript">
            var showContext = function(context) {
                console.log(context);
                $('#ac-target').text(context.target);
                $('#ac-contentId').text(context.context.contentId);
                $('#ac-contentType').text(context.context.contentType);
            };

            AP.require('navigator', function(navigator) {
                navigator.getCurrent(showContext);
            });
        </script>
    </body>
</html>
