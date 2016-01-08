<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
        <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
    </head>
    <body>
        <div id="ac-current-page-context">
        </div>
        <script type="text/javascript">
            function getContext() {
                var showContext = function(context) {
                    $('#ac-current-page-context').text(context);
                }

                AP.require('navigator', function(navigator) {
                    navigator.getCurrentContext(showContext);
                });
            }
        </script>
    </body>
</html>
