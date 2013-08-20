<!DOCTYPE html>
<html>
<head>
  <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
  <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
  <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
</head>
<body>
Panel Id: <span id="panel-id">{{panelid}}</span>
<button id="emit-button">Emit</button>
<div id="event-log"></div>
<script>
  var eventId = 0;
  window.onload = function () {
    AP.require(["_dollar", "events"], function ($, events) {
      var panelId = $("#panel-id")[0].innerHTML;
      events.on("test-event", function (pid, eid) {
        var val = pid + "-" + eid;
        $("#event-log")[0].innerHTML += "<div id='" + val + "'>" + val + "</div>";
        AP.resize();
      });
      $("#emit-button").bind("click", function () {
        events.emit("test-event", "panel-" + panelId, "event-" + (eventId += 1));
      });
    });
  };
</script>
</body>
</html>
