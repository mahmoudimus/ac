# Local add-on development

You will go through three phases during add-on development: developing and testing with a local version of the
Atlassian product, testing on a Cloud instance, and finally making the add-on available via the Atlassian Marketplace 
(as a public of private listing).

This document explains how you would go about developing your add-on with a local copy of the Atlassian product.
[Installing in the cloud](./cloud-installation.html) explains how to use private Marketplace listings to test in
cloud instances, and [Selling on  Marketplace](./selling-on-marketplace.html) explains how to release your add-on to the
public.

## Step 1. Configure your local development environment

To implement Connect add-ons and test them locally, we provide you with tools to start a local instance of the Atlassian
application, in the Atlassian Plugins SDK.
There are two ways to configure your local development environment: you can install everything on your machine, 
or to save you the hassle you can spin up an instance of our pre-configured Vagrant box.

<a data-replace-text="Local installation instructions [-]" class="aui-expander-trigger" aria-controls="install-local">Local installation instructions [+]</a>
<div id="install-local" class="aui-expander-content">
    <span data-include="/assets/includes/install-local.html"></span>
</div>

<a data-replace-text="Using our pre-configured Vagrant box [-]" class="aui-expander-trigger" aria-controls="install-vagrant">Using our pre-configured Vagrant box [+]</a>
<div id="install-vagrant" class="aui-expander-content">
    <span data-include="/assets/includes/install-vagrant.html"></span>
</div>

<a data-replace-text="More detailed information about using the Vagrant box [-]" class="aui-expander-trigger" aria-controls="instructions-vagrant">More detailed information about using the Vagrant box [+]</a>
<div id="instructions-vagrant" class="aui-expander-content">
    <span data-include="/assets/includes/instructions-vagrant.html"></span>
</div>


## Step 2. Start the local Atlassian application 

You can start a local instance of JIRA or Confluence Cloud with Atlassian Connect as follows:

<a data-replace-text="If you are using a local installation [-]" class="aui-expander-trigger" aria-controls="runproduct-local">If you are using a local installation [+]</a>
<div id="runproduct-local" class="aui-expander-content">
    <span data-include="/assets/includes/runproduct-local.html"></span>
</div>

<a data-replace-text="If you are using the Vagrant box [-]" class="aui-expander-trigger" aria-controls="runproduct-vagrant">If you are using the Vagrant box [+]</a>
<div id="runproduct-vagrant" class="aui-expander-content">
    <span data-include="/assets/includes/runproduct-vagrant.html"></span>
</div>

## Step 3. Start your add-on

Start your add-on application. The options for hosting your add-on are many, but when working on a locally hosted
environment, you can use any web framework / server you wish to build your add-on.

For an example of using a HTTP server, see the [getting started](../guides/getting-started.html) guide.

<a data-replace-text="Example using atlassian-connect-express in the Vagrant box [-]" class="aui-expander-trigger" aria-controls="demo-vagrant-ace">Example using atlassian-connect-express in the Vagrant box [+]</a>
<div id="demo-vagrant-ace" class="aui-expander-content">
    <span data-include="/assets/includes/demo-vagrant-ace.html"></span>
</div>



## Step 4. Register your add-on

Registering your add-on installs it in the Atlassian application. After installation, the add-on appears in the list of
user-installed add-ons in the [Manage Add-ons](https://confluence.atlassian.com/display/UPM/Universal+Plugin+Manager+Documentation)
page in the administration console and its features are present in the application UI.

You can install an add-on with the UPM as follows. Note, these instructions were written for UPM version 2.14 or later.

1. Log in to the Atlassian application interface as an admin or a system administrator. If you
started the application with Atlassian's SDK, the  default username/password combination is admin/admin.
2. Choose <img src="../assets/images/cog.png" alt="Settings" /> > __Add-ons__ from the menu.
The Administration page will display.
3. Choose the __Manage add-ons__ option.
4. Scroll to the page's bottom and click the __Settings__ link. The __Settings__ dialog will display.
5. Make sure the "Private listings" option is checked and click __Apply__.
6. Scroll to the top of the page and click the __Upload Add-on__ link.
7. Enter the URL to the hosted location of your plugin descriptor. In this example, the URL is
similar to the following:  `http://localhost:8000/atlassian-connect.json`. (If you are installing
to a cloud instance, the URL must be served from the Marketplace, and will look like `https://marketplace.atlassian.com/download/plugins/com.example.add-on/version/39/descriptor?access-token=9ad5037b`)
8. Press __Upload__. The system takes a moment to upload and register your plugin. It displays the
__Installed and ready to go__ dialog when installation is complete. <img width="100%" src="../assets/images/installsuccess.jpeg" />
9. Click __Close__.
10. Verify that your plugin appears in the list of __User installed add-ons__. For example, if you
used Hello World for your plugin name, that will appears in the list.



## Step 5. Test your add-on

The exact steps for testing will vary based on what your add-on does, of course. However, there are some common minimal
steps applicable to any add-on for ensuring that it got installed correctly.

After registering your add-on in the Atlassian application, it should appear in the UPM as a user-installed add-on. Any
UI features you have declared with modules should now be visible as well.


## Step 6. Change your code and reload

You can now change and reload your add-on as normal for your app or framework. Changes should show up immediately.
(Watch out for browser caching!)

You only need to re-register the add-on when you change the descriptor file, such as when modifying or adding module
declarations, scopes or changing the plugin-info details. Simply repeat step 3.

You should not need to restart the Atlassian application while developing.

<a name='utilities'></a>
## Handy tools

The following tools can be of great help when implementing add-ons!
<table class='aui'>
	<thead>
		<th>Tool</th>
		<th>Description</th>
	</thead>
	<tr>
		<td>[JSON descriptor validator](https://atlassian-connect-validator.herokuapp.com/validate)</td>
		<td>This validator will check that your descriptor is syntactically correct. Just paste the JSON content 
			of your descriptor in the "descriptor" field, and select the Atlassian product you want to validate 
			against.</td>
	</tr>
	<tr>
		<td>[JWT decoder](http://jwt-decoder.herokuapp.com/jwt/decode)</td>
		<td>An encoded JWT token can be quite opaque. You can use this handy tool to decode JWT tokens and inspect 
			their content. Just paste the full URL of the resource you are trying to access, including the JWT
			token, in the URL field. E.g. http://localhost:2990/jira/path/to/rest/endpoint?jwt=token</td>
	</tr>
</table>




