<h1 class="index-heading">Introducing Atlassian Connect</h1>

<h2 class="index-heading">Build, install and sell add-ons for JIRA and Confluence OnDemand</h1>

<div class="index-video-container">
    <a href="//fast.wistia.net/embed/iframe/3e1auia2xi?popover=true" class="wistia-popover[height=540,playerColor=205081,width=960,helpers.overlay.css.backgroundColor=#000,helpers.overlay.opacity=1,padding=20]">
        <div class="inner video-thumbnail">
            <div class="playButton"></div>
        </div>
    </a>
<script charset="ISO-8859-1" src="//fast.wistia.com/assets/external/popover-v1.js"></script>
</div>


## What is Atlassian Connect?
An Atlassian Connect add-on is any web application that extends an Atlassian application, like JIRA or Confluence. It may be an existing app that you integrate with the Atlassian app or a new service that you create to add features to an Atlassian app. Atlassian Connect add-ons operate remotely over HTTP and can be written with any programming
language and web framework.

Fundamentally, Atlassian Connect add-ons have three major capabilities. Add-ons can:

1. Insert content in [certain defined places](../modules) in the Atlassian application's UI.
2. Make calls to the Atlassian application's [REST API](../rest-apis/product-api-browser.html).
3. Listen and respond to [WebHooks](../modules/jira/webhooks.html) fired by the Atlassian application.

<div class="index-button">
<a href="/guides/getting-started.html"><button class="primary-cta aui-button aui-button-primary">Build Hello World in 10 minutes</button></a>
</div>


## Interacting with Atlassian OnDemand
While Atlassian Connect add-ons run seaparately from the Atlassian application, to an
end user, the add-on appears as a fully integrated part of the Atlassian application. 
After subscribing to the add-on, the features are delivered from within the UI and workflows of the host application.

Most Atlassian Connect add-ons will be implemented as multi-tenanted services. 
This means that a single Atlassian Connect application must take into account multiple subscribing organizations. 
For example, each add-on will maintain subscriber-specific data and configuration. 
For more about multi-tenancy design considerations, see [Add-on Design Considerations](https://developer.atlassian.com/display/AC/Add-on+Design+Considerations).

<div id="architecture-graphic">
</div>

Security is a important concern in a distributed component model such as Atlassian Connect. 
Atlassian Connect relies on HTTPS and JWT authentication to secure communication between the add-on, 
the Atlassian product instance and the end-user's browser.

Read our [security overview](../concepts/security.html) for more details.

<div class="closing-cta">
    <a href="guides/getting-started.html">
        <button class="primary-cta aui-button aui-button-primary">
            Try the Hello World example
        </button>
    </a>
    <p><a href="guides/introduction.html">or read the detailed introduction</a></p>
</div>




