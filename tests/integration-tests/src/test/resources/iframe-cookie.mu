<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <div class="ac-content">
      <button class="aui-button" id="save-cookie">Save Cookie</button>
      <button class="aui-button" id="read-cookie">Read Cookie</button>
      <button class="aui-button" id="erase-cookie">Erase Cookie</button>
      <div id="cookie-contents"></div>
    </div>
    <script>
      AP.require(["_dollar", "cookie"], function($, apCookie) {
        $("#save-cookie").bind("click", function() {
          apCookie.save('cookiename', 'cookie contents');
        });
        $("#read-cookie").bind("click", function() {
            $('#cookie-contents').html('');
            apCookie.read('cookiename', function(value){
                $('#cookie-contents').html(value);
            });
        });
        $("#erase-cookie").bind("click", function() {
            apCookie.erase('cookiename');
        });

      });
    </script>
  </body>
</html>
