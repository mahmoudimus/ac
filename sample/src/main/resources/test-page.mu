<html>
    <head>
        <script src="{{baseUrl}}/remoteapps/all.js" type="text/javascript"></script>
        <script src="/jquery-1.7.min.js" type="text/javascript"></script>
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
        <script type="text/javascript">
            RA.init();
            RA.resize(600,400);
            RA.getUser(function(result) {
                var fullNameSpan = $("<span/>").attr("id", "fullName").html(result.fullName);
                $("#user").append(fullNameSpan);
            });
        </script>
    </body>
</html>
