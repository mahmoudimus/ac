<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <h3 id="title">Dashboard item</h3>
    <div>Dashboard item id <span id="dashboardItemId">{{dashboardItemId}}</span></div>
    <div>Dashboard item id <span id="dashboardId">{{dashboardId}}</span></div>
    <div>Status: <span id="propertiesStatus"></span></div>
    <div>Status: <span id="properties"></span></div>
    <div>Edit:<span id="editBox"></span></div>
    <div><button id="set-title">Set title</button></div>
    <script>
      (function (AP) {
        // test unauthorised scope access -- resource scope not in requested permissions
        AP.request("/rest/api/2/dashboard/{{dashboardId}}/items/{{dashboardItemId}}/properties", {
          success: function (data, statusText, xhr) {
            document.getElementById("properties").innerHTML = data;
            document.getElementById("propertiesStatus").innerHTML = xhr.status;
          },
          error: function (xhr, statusText, errorThrown) {
            document.getElementById("properties").innerHTML = xhr.status;
          }
        });

        AP.require(['jira'], function (jira) {
          jira.DashboardItem.onDashboardItemEdit(function() {
            document.getElementById("editBox").innerHTML = "edit triggered";
          });
        });

        document.getElementById("set-title").addEventListener('click', function() {
          AP.require(['jira'], function(jira) {
            jira.setDashboardItemTitle("Setting title works");
          });
        });

      })(AP);
    </script>
  </body>
</html>


