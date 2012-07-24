<!doctype html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <script src="{{baseUrl}}/remoteapps/all.js"></script>
        <script>RA.init();</script>
    </head>
    <body>
        <h2>It worked!</h2>

        <h3>General Info</h3>
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
            Current user id: <span id="userId"></span>
        </div>
        <div>
            Current location: <span id="location"></span>
        </div>

        <h3>REST Response</h3>
        <div>
            Status: <span id="rest-status"></span>
        </div>
        <div>
            Status text: <span id="rest-status-text"></span>
        </div>
        <div>
            Content-Type: <span id="rest-content-type"></span>
        </div>
        <div>
            Response text: <span id="rest-response-text"></span>
        </div>
        <div>
            Data: <span id="rest-data"></span>
        </div>

        <h3>Links</h3>
        <a href="">Reload Host Page</a><br>
        <a href="/">Host Context Root</a><br>
        <a href="foo">Relative Foo</a><br>
        <a href="remoteAppGeneral/foo">App Foo</a><br>

        <h3>Images (Resize Test)</h3>
        <img src="sandcastles.jpg">

        <script src="jquery-1.7.min.js" type="text/javascript"></script>
        <script type="text/javascript">
          (function () {
            // general api testing

            RA.getUser(function(user) {
              $("#user").text(user.fullName);
              $("#userId").text(user.id);
            });

            RA.getLocation(function(location) {
              $("#location").text(location);
            });

            // basic request api testing

            function bindXhr(xhr) {
              $("#rest-status").text(xhr.status);
              $("#rest-status-text").text(xhr.statusText);
              $("#rest-content-type").text(xhr.getResponseHeader("content-type"));
              $("#rest-response-text").text(xhr.responseText);
            }

            RA.request("/rest/remoteapptest/1/user", {
              success: function (data, statusText, xhr) {
                $("#rest-data").text(data);
                bindXhr(xhr);
              },
              error: function (xhr, statusText, errorThrown) {
                bindXhr(xhr);
              }
            });
          }());
        </script>
    </body>
</html>
