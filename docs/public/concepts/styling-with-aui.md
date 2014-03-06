# Styling Add-ons with Atlassian UI

The Atlassian User Interface Library (AUI) is the framework that Atlassian uses to build our
products. AUI is a library of Javascript, CSS, templates and other resources you can include
in your projects. Building a user interface with AUI automatically ensures your add-on will
match its target application's user interface.

Because style properties from the parent page don't cascade down to your add-on's iframe,
you'll need to control your page's design to match the parent. Using the AUI flat-pack
will help you with this task.

## Using AUI styling in your pages

You can include the AUI resources on any page that you serve from your add-on. In most cases, you
should use the most recent version of AUI that is available. It's your responsiblity to update as
new versions become available. Not only will you get new features and bug fixes, but using the most
recent version of AUI ensures that your styles will most closely match those of your host application.

Use the HTML below to include the AUI resources on your page, directly from the CDN:

#### CSS
```
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.4.3/aui/css/aui.css" media="all">
<!--[if lt IE 9]><link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.4.3/aui/css/aui-ie.css" media="all"><![endif]-->
<!--[if IE 9]><link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.4.3/aui/css/aui-ie9.css" media="all"><![endif]-->
```

#### Javascript
```
<script src="//cdnjs.cloudflare.com/ajax/libs/aui/5.4.3/aui/js/aui-all.js" type="text/javascript"></script>
```

For more details on AUI, read the [AUI documentation](https://developer.atlassian.com/display/AUI/).

An easy way to get started with AUI is to experiment with the [sandbox](https://docs.atlassian.com/aui/latest/sandbox/).


