<html>
    <head>
        <script src="{{baseUrl}}/remoteapps/all.js" type="text/javascript"></script>
        <script src="jquery-1.7.min.js" type="text/javascript"></script>
    </head>
    <body>
        <div>
            Current user: <span id="user"></span>
        </div>
        <div>
            Was Submitted: <span id="submitted">false</span>
        </div>

        <script type="text/javascript">
            RA.init();
            RA.getUser(function(result) {
                $("#user").text(result.fullName);
            });
            RA.Dialog.onSubmit(function() {
                if ($("#submitted").text() == "false") {
                   $("#submitted").text("true");
                   return false;
                }
                else
                {
                   return true;
                }
            });
        </script>
    </body>
</html>
