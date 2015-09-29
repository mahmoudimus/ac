<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div class="ac-content">
      <button class="aui-button" id="display-message">Display Message</button>
    </div>
    <script>
      AP.require(["_dollar", "messages"], function($, messages) {
        $("#display-message").bind("click", function() {
          messages.info("plain text title", "plain text body");
        });
      });
    </script>
  </body>
</html>
