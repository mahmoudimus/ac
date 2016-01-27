<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
</head>
<body>
<script type="text/javascript">
    AP.require('dialog', function(dialog) {
        dialog.createButton('Launch Fullscreen Dialog').bind(function() {
            dialog.create({
                key: 'multipleDialogs2Dialog',
                size: 'fullscreen',
                header: 'Fullscreen Dialog title'
            });
        });
    });
</script>
<h1 id="dialog-name">Dialog1</h1>
<p>
    This dialog is modal and might represent (say) the Confluence Macro Browser dialog that launches a full-screen
    designer dialog.
</p>
<p>
    Press the 'Launch Fullscreen Dialog' button to see the fullscreen dialog with a control bar at the top.
</p>
</body>
</html>
