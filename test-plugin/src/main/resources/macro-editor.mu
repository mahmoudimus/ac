<html>
    <head>
        <link rel="stylesheet" type="text/css" href="{{baseUrl}}/remoteapps/all.css">
        <script src="{{baseUrl}}/remoteapps/all.js" type="text/javascript"></script>
        <script src="public/jquery-1.7.min.js" type="text/javascript"></script>
    </head>
    <body>
        <select id="footy">
            <option name="American Football">American Football</option>
            <option name="Soccer">Soccer</option>
            <option name="Rugby Union">Rugby Union</option>
            <option name="Rugby League">Rugby League</option>
        </select>

        <script type="text/javascript">
            RA.init();

            RA.Dialog.onDialogMessage("submit", function() {
                var footy = $("#footy");
                return {
                    result: true,
                    macroParameters: {
                        footy: footy.val()
                    }
                }
            });
        </script>
    </body>
</html>