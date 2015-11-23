<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <h1>Simple Macro</h1>
    <h2>Request Info</h2>
    <div>Url: <span class="req_url">{{req_url}}</span></div>
    <div>Uri: <span class="req_uri">{{req_uri}}</span></div>
    <div>HTTP Method: <span class="req_method">{{req_method}}</span></div>
    <div>Query String: <span class="req_query">{{req_query}}</span></div>
    
    <h2>Macro Context Parameters</h2>
    <div>Output Type: <span class="output_type">{{output_type}}</span></div>
    <div>Page ID: <span class="page_id">{{page_id}}</span></div>
    <div>Page Type: <span class="page_type">{{page_type}}</span></div>
    <div>Page Title: <span class="page_title">{{page_title}}</span></div>
    <div>Space Key: <span class="space_key">{{space_key}}</span></div>
    <div>User ID: <span class="user_id">{{user_id}}</span></div>
    <div>User Key: <span class="user_key">{{user_key}}</span></div>
</body>
</html>