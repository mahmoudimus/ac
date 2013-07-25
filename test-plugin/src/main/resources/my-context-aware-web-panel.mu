<!-- This web panel is used by a number of remote-web-panels in atlassian-plugin-jira and atlassian-plugin-confluence -->
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseUrl}}/remotable-plugins/all.css">
    <script src="{{baseUrl}}/remotable-plugins/all-debug.js" type="text/javascript"></script>
    <script src="public/jquery-1.7.min.js" type="text/javascript"></script>
  </head>
  <body>
    {{#user_id}}
    <div>User id <span id="user_id">{{user_id}}</span></div>
    {{/user_id}}
    {{#project_id}}
    <div>Project id <span id="project_id">{{project_id}}</span></div>
    {{/project_id}}
    {{#issue_id}}
    <div>Issue id <span id="issue_id">{{issue_id}}</span></div>
    {{/issue_id}}
    {{#page_id}}
    <div>Page id <span id="page_id">{{page_id}}</span></div>
    {{/page_id}}
    {{#space_id}}
    <div>Space id <span id="space_id">{{space_id}}</span></div>
    {{/space_id}}
    {{#profile_user_key}}
    <div>Profile user key <span id="profile_user_key">{{profile_user_key}}</span></div>
    {{/profile_user_key}}
    {{#profile_user_name}}
    <div>Profile user name <span id="profile_user_name">{{profile_user_name}}</span></div>
    {{/profile_user_name}}

  </body>
</html>