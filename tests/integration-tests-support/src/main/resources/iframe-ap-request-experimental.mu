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

    <table class="aui">
      <thead>
        <tr>
          <th>Test Case #</th>
          <th>Status Code</th>
          <th>Status Text</th>
          <th>Response</th>
        </tr>
      </thead>
      <tbody id="test-results">
      </tbody>
    </table>

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

        function displayResults(xhr, index) {
          var row = document.createElement("tr");

          // first cell - test case number
          var c1 = document.createElement("td");
          c1.appendChild(document.createTextNode(index));
          row.appendChild(c1);

          // second cell - response status
          var c2 = document.createElement("td");
          c2.appendChild(document.createTextNode(xhr.status));
          c2.setAttribute("id", "client-http-status-" + index);
          row.appendChild(c2);

          // third cell - response status text
          var c3 = document.createElement("td");
          c3.appendChild(document.createTextNode(xhr.statusText));
          c3.setAttribute("id", "client-http-status-text-" + index);
          row.appendChild(c3);

          // fourth cell - response text
          var c4 = document.createElement("td");
          c4.appendChild(document.createTextNode(xhr.responseText));
          c4.setAttribute("id", "client-http-response-text-" + index);
          row.appendChild(c4);

          document.getElementById("test-results").appendChild(row);
        }

        AP.request("/rest/remoteplugintest/1/experimental/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            displayResults(xhr, 1);
          },
          error: function (xhr, statusText, errorThrown) {
            displayResults(xhr, 1);
          }
        });

        AP.request("/rest/remoteplugintest/1/experimental/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            displayResults(xhr, 2);
          },
          error: function (xhr, statusText, errorThrown) {
            displayResults(xhr, 2);
          },
          experimental: true
        });

        AP.request("/rest/remoteplugintest/1/experimental/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            displayResults(xhr, 3);
          },
          error: function (xhr, statusText, errorThrown) {
            displayResults(xhr, 3);
          },
          experimental: false
        });

        AP.request("/rest/remoteplugintest/1/experimental/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            displayResults(xhr, 4);
          },
          error: function (xhr, statusText, errorThrown) {
            displayResults(xhr, 4);
          },
          experimental: "true"
        });

        AP.request("/rest/remoteplugintest/1/experimental/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            displayResults(xhr, 5);
          },
          error: function (xhr, statusText, errorThrown) {
            displayResults(xhr, 5);
          },
          experimental: "t"
        });

        AP.request("/rest/remoteplugintest/1/experimental/user?rnd=" + Math.random(), {
          headers: {
            "Accept": "application/json"
          },
          success: function (data, statusText, xhr) {
            displayResults(xhr, 6);
          },
          error: function (xhr, statusText, errorThrown) {
            displayResults(xhr, 6);
          },
          experimental: 1
        });


      })(AP);
    </script>
  </body>
</html>


