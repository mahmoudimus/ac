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
            <button id="navigate-to-page" onclick="navigateToPage()">Navigate to page</button>
            <button id="navigate-to-edit-page" onclick="navigateToEditPage()">Navigate to edit page</button>
            <button id="navigate-to-user-profile" onclick="navigateToUserProfile()">Navigate to user profile</button>
            <button id="navigate-to-space" onclick="navigateToSpace()">Navigate to space</button>
            <button id="navigate-to-space-tools" onclick="navigateToSpaceTools()">Navigate to space tools</button>
        </div>
        <script type="text/javascript">
            function navigateToDashboard() {
                AP.require('navigator', function(navigator) {
                    navigator.go("dashboard");
                });
            }
        </script>
        <script type="text/javascript">
            function navigateToPage() {
                AP.require('navigator', function(navigator) {
                    navigator.go("contentview", {contentId: {{{contentId}}}});
                });
            }
        </script>
        <script type="text/javascript">
            function navigateToEditPage() {
                AP.require('navigator', function(navigator) {
                    navigator.go("contentedit", {contentType: "page", contentId: {{{contentId}}}});
                });
            }
        </script>
        <script type="text/javascript">
            function navigateToUserProfile() {
                AP.require('navigator', function(navigator) {
                    navigator.go("userprofile", {username: "admin"});
                });
            }
        </script>
        <script type="text/javascript">
            function navigateToSpace() {
                AP.require('navigator', function(navigator) {
                    navigator.go("spaceview", {spaceKey: "{{{spaceKey}}}"});
                });
            }
        </script>
        <script type="text/javascript">
            function navigateToSpaceTools() {
                AP.require('navigator', function(navigator) {
                    navigator.go("spacetools", {spaceKey: "{{{spaceKey}}}"});
                });
            }
        </script>
    </body>
</html>
