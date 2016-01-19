<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
  </head>
  <body>
    <script type="text/javascript">
      AP.require(["confluence"], function (confluence) {
          confluence.getMacroData(function(currentParams) {
            if (currentParams && currentParams['param1']) {
                confluence.saveMacro({
                    param1: currentParams['param1'] + currentParams['param1']
                });
              } else {
                confluence.saveMacro({
                    param1: "ThisIsMyGreatNewParamValue"
                });
              }
          });
      });
    </script>
  </body>
</html>

