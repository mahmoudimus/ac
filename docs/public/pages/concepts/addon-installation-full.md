# Installing your Add-on
You can install add-ons using the add-on manager for Atlassian applications, the [Universal Plugin Manager (UPM)](https://confluence.atlassian.com/display/UPM/Universal+Plugin+Manager+Documentation). With the UPM, you can either register the add-on through the UI, similar to how an administrator would, or using UPM's REST API. After registration, the add-on appears in the list of user-installed add-ons in the [Manage Add-ons](https://confluence.atlassian.com/display/UPM/Universal+Plugin+Manager+Documentation) page in the administration console and its features are available for use in the target application. 

## Installing an add-on using the Universal Plugin Manager

<div class="diagram">
participant Admin
participant Add_on_server
participant OnDemand
Admin->OnDemand:Install add-on descriptor through UPM
OnDemand->Add_on_server:info about OnDemand instance\nincluding shared keys
OnDemand->Admin:Notified of successful installation 
</div>

Installing your add-on adds it to your OnDemand application. To be more precise, installing is really just registering the add-on with the application and the only thing that is stored by the application at this time is the add-on descriptor. 

You can install an add-on with the UPM as follows. Note, these instructions were written for UPM version 2.14 or later.

 1. Log in to the Atlassian application interface as an admin or a system administrator. If you started the application with Atlassian's SDK, the  default username/password combination is admin/admin.
 2. Choose <img src="../../assets/images/cog.png" alt="Settings" /> > __Add-ons__ from the menu. The Administration page will display.
 3. Choose the __Manage add-ons__ option.
 4. Scroll to the page's bottom and click the __Settings__ link. The __Settings__ dialog will display. 
 5. Make sure the "Private listings" option is checked and click __Apply__.
 6. Scroll to the top of the page and click the __Upload Add-on__ link.
 7. Enter the URL to the hosted location of your plugin descriptor. In this example, the URL is similar to the following:  `http://localhost:8000/atlassian-connect.json`. (If you are installing to an OnDemand instance, the URL must be served from the Marketplace, and will look like `https://marketplace.atlassian.com/download/plugins/com.example.add-on/version/39/descriptor?access-token=9ad5037b`)
 8. Press __Upload__. The system takes a moment to upload and register your plugin. It displays the __Installed and ready to go__ dialog when installation is complete. <img width="100%" src="../../assets/images/installsuccess.jpeg" />
 9. Click __Close__.
 10. Verify that your plugin appears in the list of __User installed add-ons__. For example, if you used Hello World for your plugin name, that will appears in the list.

## Installing an add-on using the REST API
You can also install an add-on using the UPM's REST API. You'll find this method useful if you want to install add-ons programmatically, say from a script, or simply want to quickly install an add-on from the command line. Broadly speaking, installing an add-on (or performing any operation against the REST API of the UPM) is a two-step process:

First get a UPM token.

Next, issue the request to the REST API, including the token you received.

The following steps walk you through these steps in detail:
Send a GET request to the following resource: 
`http://HOST_NAME:PORT/CONTEXT/rest/plugins/1.0/?os_authType=basic`
In your request:

 1. Replace `HOST_NAME` and `PORT` with the actual host name and port of the target Atlassian application. If applicable (i.e., if using a development instance), include the `CONTEXT` with the application context (/jira or /confluence).
 2. Include the username and password for a system administrator user in the target Atlassian application as HTTP Basic Authentication credentials.
 3. Set the Accept header for the request to: "`application/vnd.atl.plugins.installed+json`"

Capture the header named "upm-token" in the response. This is the UPM token you need for the next request.
Now install your add-on by issuing a POST request to the following resource:
`http://HOST_NAME:PORT/CONTEXT/rest/plugins/1.0/?token=${upm-token}`
In your request:

 1. Again use the actual host name and port and path for your target Atlassian application.
 2. The token value should be the value of the upm-token header you just received.
 3. In the request, set the Accept header to: "`application/json`"
 4. Set the Content-Type for the data to: "`application/vnd.atl.plugins.install.uri+json`"
 5. In the body of the POST, include the following JSON data:

This registers the add-on declared by the `atlassian-connect.json` file at the URL.

Note that you should not rely on the response returned from the POST request to confirm that the plugin has been installed. Instead, the best way to confirm that the plugin has been installed is to add a lifecycle event to your add-on descriptor that listens for the add-on installation event. The lifecycle declaration in the `atlassian-plugin.json` file would look something like this:
```
{
    "name": "My Addon",
    "lifecycle": {
        "installed" : "/your-url-here"
    }
}
```

## Troubleshooting authentication
When registering from the command line using [cURL](http://curl.haxx.se/docs/manpage.html), keep in mind that cURL does not perform session maintenance across calls (unlike other clients, such as Apache HttpClient). Thus, you need to either:

Send the authentication credentials in both requests, or

Have cURL save any cookies from the first request and send them in the second. That is:

 1. To save cookies, use the -c switch: `curl -c cookiesfile.txt` ...
 2. And then include the cookies in the second request: `curl -b cookiesfile.txt` ...
