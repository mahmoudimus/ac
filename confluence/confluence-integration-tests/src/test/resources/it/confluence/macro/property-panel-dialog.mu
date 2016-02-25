<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <script type="text/javascript">
      AP.require(["dialog"], function (dialog) {
          dialog.create({
              key: 'dialog-key',
              width: '500px',
              height: '200px',
              chrome: true
            });
      });
    </script>
  </body>
</html>

