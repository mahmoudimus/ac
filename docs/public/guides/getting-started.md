# Getting Started

The first step in learning how to develop Atlassian Connect add-ons is to get acquainted with the basic building blocks of an Atlassian Connect add-on.

## Who should read the Getting Started?
This is the place to start if you're just getting to know Atlassian Connect. This page (and its child pages), introduce you to the basic building blocks of Atlassian Connect. It does so by taking you through an example add-on, introducing you to many of the concepts and requirements for building Atlassian Connect add-ons along the way.

The Getting Started topics focus on local development of Atlassian Connect add-ons. If you're building an Atlassian Connect add-on for personal use and don't intend to make it available on the Atlassian Marketplace, you'll need to go no further. But if you do decide to take it to the next level, for example, by making your add-on a paid-via-Atlassian add-on, you'll need a live Atlassian OnDemand instance and an account on the Atlassian Marketplace for further testing.

But we'll leave that for later. For now, it's just you and your laptop.

## Choosing your tools
Atlassian Connect is not itself a development framework in the traditional sense. More precisely, it's the development model, tools, and backend support for building web-based, distributed add-ons for Atlassian applications.

Because Atlassian Connect add-ons operate remotely from the OnDemand instance, you can use any web framework, language, web framework, database, or library you like to create the add-ons. Whether it's Node, Play, Rails, Django, or something else, the framework you use only needs to be well suited for building web applications and REST development works for an add-on implementation.

While Atlassian Connect doesn't comprise a development framework, we've contributed to existing frameworks to make them easier to use to develop for Atlassian Connect. These include:

 * [atlassian-connect-play-java](https://bitbucket.org/atlassian/atlassian-connect-play-java) is a toolkit for building Atlassian Connect add-ons for the Java-based [Play Framework](http://www.playframework.com/).
 * [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) is a library for developing Atlassian Connect add-ons using Node.js for the [Express](http://expressjs.com/) web framework .

And keep your eye on this page for new ones.

## Supported versions of Atlassian applications
Atlassian Connect relies on some infrastructure components to be present in the Atlassian application platform. Not all versions of the application has these. An Atlassian Connect add-on works with these versions of the product: 

 * Confluence 5.1 and later.
 * JIRA 6.0, and later.

## Sample add-ons
Studying sample applications is a great way to get to know Atlassian Connect development. We've assembled a set of sample application for your interest. 

 * [Sequence Sequence Diagram](https://bitbucket.org/atlassianlabs/atlassian-connect-confluence-sequence-diagramr): a Confluence remote macro written with the [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) library and the [Express](http://expressjs.com/) Node.js web framework.
 * [Pastebin](https://bitbucket.org/sleberrigaud/pastebin-remoteapp-play2-java): a simple Confluence add-on written using the Play Framework.
 * [TaskMaster](https://bitbucket.org/mrdon/taskmaster-plugin/wiki/Home): a JIRA add-on that allows quick subtask creation within the issue detail page. Written in JavaScript using the [atlassian-connect-express](https://bitbucket.org/atlassian/atlassian-connect-express) library and the [Express](http://expressjs.com/) framework.
And we're adding more samples all the time. Check the [Sample Add-ons](https://developer.atlassian.com/display/AC/Sample+Add-ons) page for the latest.

## Forward!
Now that you've gotten acquainted with Atlassian Connect, you're ready to explore the other Getting Started topics and start developing with Atlassian Connect:

 * [Hello World](hello-world.html)
 * [Add-on Descriptor](addon-descriptor.html)
 * [Installing an Add-on](installing-an-addon.html)
 * [JWT](jwt.html)

