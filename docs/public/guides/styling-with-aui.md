# Styling Add-ons with Atlassian UI

Atlassian Connect provides a small set of CSS styles for styling your pages.

Note that this stylesheet is not the one associated with the [Atlassian User Interface (AUI)](https://developer.atlassian.com/design/) or a subset of AUI. To style your pages using AUI and the Atlassian Design Guidelines, you should reference the AUI flat-pack into your add-on using the [CDN](http://cdnjs.com/libraries/aui/) or bundle it into your add-on. But if you just need a minimal set of styles, you can add the following to your HTML:
```
<link rel="stylesheet" type="text/css" href="https://{OnDemand hostname}/{context}/atlassian-connect/all.css"/>
```

Because style properties from the parent don't cascade down to the page in the iframe, it's important to understand that you'll need to control your page's design to match the parent. Utilizing the AUI flat-pack is going to help you with this task.

## Using AUI styling in your pages

The most convenient way of styling your pages to look like atlassian products is to use the AUI css and javascript.

```
<link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/css/aui.css" media="all">
<!--[if lt IE 9]><link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/css/aui-ie.css" media="all"><![endif]-->
<!--[if IE 9]><link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/css/aui-ie9.css" media="all"><![endif]-->
```

```
<script src="//cdnjs.cloudflare.com/ajax/libs/aui/5.2-m6/js/aui-all.js" type="text/javascript"></script>
```

For more details on AUI, you should view the [AUI documentation](https://developer.atlassian.com/display/AUI/).

An easy way to get started with AUI is to use the [sandbox](https://docs.atlassian.com/aui/latest/sandbox/)