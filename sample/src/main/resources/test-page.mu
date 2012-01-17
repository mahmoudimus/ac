<html>
    <head>
        <script src="{{baseUrl}}/remoteapps/all.js" type="text/javascript"></script>
    </head>
    <body>
        <h2>It worked!</h2>
        <div>
            Message: <span id="message">Success</span>
        </div>
        <div>
            Host Consumer Key: <span id="consumerKey">{{consumerKey}}</span>
        </div>
        <script type="text/javascript">
            RA.init();
            RA.resize(600,400);
        </script>
    </body>
</html>