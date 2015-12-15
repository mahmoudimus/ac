<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
        <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
    </head>
    <body>
        <div class="ac-content">
            <button id="navigate-to-dashboard" onclick="navigateToDashboard()">Navigate to dashboard</button>
        </div>
        <script type="text/javascript">
            function navigateToDashboard() {
                AP.require('navigator', function(navigator) {
                    navigator.go("dashboard");
                });
            }
        </script>
    </body>
</html>
