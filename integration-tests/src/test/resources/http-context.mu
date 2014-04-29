<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
<h2>Request Info</h2>
<div>Url: <span class="req_url">{{req_url}}</span></div>
<div>Uri: <span class="req_uri">{{req_uri}}</span></div>
<div>HTTP Method: <span class="req_method">{{req_method}}</span></div>
<div>Query String: <span class="req_query">{{req_query}}</span></div>

<div>Client Key: <span class="clientKey">{{clientKey}}</span></div>
<div>Locale: <span class="locale">{{locale}}</span></div>
<div>License Status: <span class="licenseStatus">{{licenseStatus}}</span></div>
<div>Time Zone: <span class="timeZone">{{timeZone}}</span></div>
</body>
</html>


