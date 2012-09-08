<!doctype html>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <link rel="stylesheet" type="text/css" href="{{baseurl}}/remoteapps/all.css">
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

        <h3>HostHttpClient Response</h3>
        <div>
            Status: <span id="server-http-status">{{httpGetStatus}}</span>
        </div>
        <div>
            Status text: <span id="server-http-status-text">{{httpGetStatusText}}</span>
        </div>
        <div>
            Content-Type: <span id="server-http-content-type">{{httpGetContentType}}</span>
        </div>
        <div>
            Entity: <span id="server-http-entity">{{httpGetEntity}}</span>
        </div>

        <h3>RA.request() Response</h3>
        <div>
            Status: <span id="client-http-status"></span>
        </div>
        <div>
            Status text: <span id="client-http-status-text"></span>
        </div>
        <div>
            Content-Type: <span id="client-http-content-type"></span>
        </div>
        <div>
            Response text: <span id="client-http-response-text"></span>
        </div>
        <div>
            Data: <span id="client-http-data"></span>
        </div>

        <h3>Links</h3>
        <a href="">Reload Host Page</a><br>
        <a href="/">Host Context Root</a><br>
        <a href="foo">App-Relative Foo</a><br>
        <a href="myAdmin/foo">Page-Relative Foo</a><br>

        <h3>Images (Resize Test)</h3>
        <img src="public/sandcastles.jpg">

        <script src="public/jquery-1.7.min.js" type="text/javascript"></script>
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
              $("#client-http-status").text(xhr.status);
              $("#client-http-status-text").text(xhr.statusText);
              $("#client-http-content-type").text(xhr.getResponseHeader("content-type"));
              $("#client-http-response-text").text(xhr.responseText);
            }
            RA.request("/rest/remoteapptest/1/user", {
              success: function (data, statusText, xhr) {
                $("#client-http-data").text(data);
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
