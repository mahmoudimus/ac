<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all.js" type="text/javascript" data-options="sizeToParent:true"></script>
  </head>
  <body>
    <div class="ac-content">
        <button id="forward">Forward</button>
        <button id="back">Backward</button>
        <button id="pushstate">Push state</button>
        <button id="clearlog">Clear log</button>

        <div id="log"></div>
    </div>
    <script type="text/javascript">

    AP.require(["history", "_dollar"], function(history, $){
        var i = 0;
        history.popState(function(e){
            $("#log")[0].innerHTML = '<div class="newurl">' + e.newURL + "</div>" +
            '<div class="oldurl">' + e.oldURL + "</div>";
        });

        $("#clearlog").bind("click", function(){
            $("#log")[0].innerHTML = '';
        });
        $("#pushstate").bind("click", function(){
            history.pushState("mypushedstate" + i);
            $("#log")[0].innerHTML = 'history pushstate' + i;
            i++;
        });
        $("#back").bind("click", function(){
            setTimeout(function(){
                history.back();
                $("#log")[0].innerHTML = 'back';
            }, 200);

        });
        $('#forward').bind("click", function(){
            history.forward();
            $("#log")[0].innerHTML = 'forward';
        });
    });
    </script>
  </body>
</html>
