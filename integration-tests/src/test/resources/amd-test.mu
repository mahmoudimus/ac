<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <table>
      <tr>
        <td align="right">env</td>
        <td id="amd-env"></td>
      </tr>
      <tr>
        <td align="right">request</td>
        <td id="amd-request"></td>
      </tr>
      <tr>
        <td align="right">dialog</td>
        <td id="amd-dialog"></td>
      </tr>
      <tr>
        <td align="right">events</td>
        <td id="amd-events"></td>
      </tr>
    </table>
    <script>
        AP.require(["env", "request", "dialog", "events"], function (env, request, dialog, events) {
          // check that modules were returned and have some of the expected values set on them
          document.getElementById("amd-env").innerHTML = (!!env && !!env.getUser).toString();
          document.getElementById("amd-request").innerHTML = (!!request && !!request.__target__).toString();
          document.getElementById("amd-dialog").innerHTML = (!!dialog && !!dialog.getButton).toString();
          document.getElementById("amd-events").innerHTML = (!!events && !!events.on).toString();
        });
    </script>
  </body>
</html>
