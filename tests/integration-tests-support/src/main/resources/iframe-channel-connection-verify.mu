<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <script>
  AP.getLocation(function(location){
    document.getElementById('channel-connected-message').innerHTML = 'Connected with connect channel!';
  });
  </script>
  <body>
  <p id="channel-connected-message"></p>
  </body>
</html>
