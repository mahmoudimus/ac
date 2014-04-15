<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript" data-options="sizeToParent:true"></script>
  </head>
  <body>
    <div class="ac-content">
        <button id="forward">Forward</button>
        <button id="back">Backward</button>
        <button id="pushstate">Push state</button>

        <div id="log"></div>
    </div>
    <script type="text/javascript">

    AP.require(["history", "_dollar"], function(history, $){
        var i = 0;
        history.popState(function(e){
            $("#log")[0].innerHTML = '<div class="newurl">' + e.newURL + "</div>" +
            '<div class="oldurl">' + e.oldURL + "</div>";
        });
        $("#pushstate").bind("click", function(){
            history.pushState("mypushedstate" + i);
            i++;
        });
        $("#back").bind("click", function(){
            history.back();
        });
        $('#forward').bind("click", function(){
            history.forward();
        });
    });
    </script>
  </body>
</html>
