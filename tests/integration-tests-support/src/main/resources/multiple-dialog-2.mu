<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <link rel="stylesheet" type="text/css" href="{{baseurl}}/atlassian-connect/all.css">
    <script src="{{baseurl}}/atlassian-connect/all-debug.js" type="text/javascript"></script>
</head>
<body>
<h1 id="dialog-name">Dialog2</h1>
<p>
    This content represents the place that a full-screen designer would go. It should contain a control (e.g.
    a simple form) capable of being modified and checked for dirty/clean state.
</p>
<p>
    At the top of this screen is the full-screen dialog control bar, with "Submit" and "Cancel" buttons at the top right.
</p>
<ul>
    <li>
        Clicking the "Submit" button should trigger submit logic registered in the script tag of this resource
        (multiple-dialog-2.mu). There may be default submit logic that makes use of a "dirty editor" API at some point.
    </li>
    <li>
        Clicking the "Cancel" button should also trigger logic in this resource's script tag.
    </li>
</ul>
</body>
</html>
