# Styling Add-ons with Atlassian UI

The Atlassian User Interface Library (AUI) is the framework that Atlassian uses to build our products. AUI is a library
 of Javascript, CSS, templates and other resources you can include in your projects. Building a user interface with
 AUI automatically ensures your feature is ADG compliant in terms of look, feel and control behavior. Anything that
 you find in the Atlassian Design Guidelines (ADG) you can build with the AUI library.

Because style properties from the parent don't cascade down to the page in the iframe, it's important to understand that
you'll need to control your page's design to match the parent. Utilizing the AUI flat-pack is going to help you with
this task.

## Using AUI styling in your pages

The most convenient way of styling your pages to look like Atlassian products is to use the AUI CSS and Javascript. You can
include these resources on any page that you serve. In most cases, you should use the most recent version of AUI that is
available. It's your responsiblity to update as new versions become available. Not only will you get new features and bug
fixes, but using the most recent version of AUI ensures that your styles will most closely match those of your host application.

#### CSS
```
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/css/aui.css" media="all">
<!--[if lt IE 9]><link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/css/aui-ie.css" media="all"><![endif]-->
<!--[if IE 9]><link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/css/aui-ie9.css" media="all"><![endif]-->
```

#### Javascript
```
<script src="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/js/aui-all.js" type="text/javascript"></script>
```

For more details on AUI, you should view the [AUI documentation](https://developer.atlassian.com/display/AUI/).

An easy way to get started with AUI is to experiment with the [sandbox](https://docs.atlassian.com/aui/latest/sandbox/).

## Minimal styling

Atlassian Connect provides a small set of CSS styles for styling your pages, but this stylesheet is not the
[Atlassian User Interface (AUI)](https://developer.atlassian.com/design/). To style your pages using AUI and the
ADG, you should reference the AUI flat-pack into your add-on using the [CDN](http://cdnjs.com/libraries/aui/).
But if you just need a minimal set of styles, you can add the following to your HTML:
```
<link rel="stylesheet" type="text/css" href="https://{OnDemand hostname}/{context}/atlassian-connect/all.css"/>
```

