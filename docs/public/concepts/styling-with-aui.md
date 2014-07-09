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
should use the most recent version of AUI that is available. It's your responsibility to update as
new versions become available. Not only will you get new features and bug fixes, but using the most
recent version of AUI ensures that your styles will most closely match those of your host application.

To include AUI resources from the Atlassian CDN, please read the
[AUI documentation](https://docs.atlassian.com/aui/latest/).

