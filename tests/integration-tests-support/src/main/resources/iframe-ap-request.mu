<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <h3>General Info</h3>
    <div>Message:<span id="message">Success</span></div>
    <div>User:<span id="user"></span></div>
    <div>User ID:<span id="userId"></span></div>
    <div>Host consumer key: <span id="consumerKey">{{clientKey}}</span></div>
    <div>Current locale: <span id="locale">{{locale}}</span></div>
    <div>Current time zone: <span id="timeZone"></span></div>
    <div>Current time zone (from the template context): <span id="timeZoneFromTemplateContext">{{timeZone}}</span></div>

    <h3>AP.request() Response</h3>
    <div>Status: <span id="client-http-status"></span></div>
    <div>Status text: <span id="client-http-status-text"></span></div>
    <div>Content-Type: <span id="client-http-content-type"></span></div>
    <div>Response text: <span id="client-http-response-text"></span></div>
    <div>Unauthorized response code: <span id="client-http-unauthorized-code"></span></div>
    <div>Text data: <span id="client-http-data"></span></div>
    <div>JSON data: <pre id="client-http-data-json"></pre></div>
    <div>XML data: <pre id="client-http-data-xml"></pre></div>

    <script>
      (function (AP) {
        // general api testing
        AP.getUser(function(user) {
          document.getElementById("user").innerHTML = user.fullName;
          document.getElementById("userId").innerHTML = user.id;
        });

        AP.getTimeZone(function(timeZone) {
          document.getElementById("timeZone").innerHTML = timeZone;
        });

        AP.getLocation(function(location) {
          document.getElementById("location").innerHTML = location;
        });

        // basic request api testing
        function bindXhr(xhr) {
          document.getElementById("client-http-status").innerHTML = xhr.status;
          document.getElementById("client-http-status-text").innerHTML = xhr.statusText;
          document.getElementById("client-http-content-type").innerHTML = xhr.getResponseHeader("content-type");
          document.getElementById("client-http-response-text").innerHTML = xhr.responseText;
        }

        AP.request("/rest/remoteplugintest/1/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "text/plain"
          },
          success: function (data, statusText, xhr) {
            document.getElementById("client-http-data").innerHTML = data;
            bindXhr(xhr);
          },
          error: function (xhr) {
            bindXhr(xhr);
          }
        });

        // additional media type requests; using timeouts to work around jq cachebuster ms timestamps
        AP.request("/rest/remoteplugintest/1/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            document.getElementById("client-http-data-json").innerHTML = data;
            AP.resize();
          },
          error: function (xhr, statusText, errorThrown) {
            console.error(xhr, statusText, errorThrown);
          }
        });

        AP.request("/rest/remoteplugintest/1/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/xml"
          },
          success: function (data, statusText, xhr) {
            document.getElementById("client-http-data-xml").innerHTML = data
              .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
            AP.resize();
          },
          error: function (xhr, statusText, errorThrown) {
            console.error(xhr, statusText, errorThrown);
          }
        });

        // test unauthorised scope access -- resource scope not in requested permissions
        AP.request("/rest/remoteplugintest/1/unauthorisedscope", {
          success: function (data, statusText, xhr) {
            document.getElementById("client-http-unauthorized-code").innerHTML = xhr.status;
            AP.resize();
          },
          error: function (xhr, statusText, errorThrown) {
            document.getElementById("client-http-unauthorized-code").innerHTML = xhr.status;
            AP.resize();
          }
        });
      })(AP);
    </script>
  </body>
</html>


