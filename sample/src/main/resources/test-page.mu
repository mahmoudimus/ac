<html>
    <head>
        <script src="{{baseUrl}}/remoteapps/all.js" type="text/javascript"></script>
        <script src="jquery-1.7.min.js" type="text/javascript"></script>
    </head>
    <body>
        <h2>It worked!</h2>
        <div>
            Message: <span id="message">Success</span>
        </div>
        <div>
            Host Consumer Key: <span id="consumerKey">{{consumerKey}}</span>
        </div>
        <div>
            Current user: <span id="user"></span>
        </div>
        <div>
            Current location: <span id="location"></span>
        </div>

        <script type="text/javascript">
            RA.init();
            RA.getUser(function(result) {
                $("#user").text(result.fullName);
            });
            RA.getLocation(function(result) {
                $("#location").text(result);
            });
            RA.Dialog.onSubmit(function() {
                console.log("calling submit handler!");
                return true;
            });
        </script>
    </body>
</html>
