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
 2. Choose <img src="../assets/images/cog.png" alt="Settings" /> > __Add-ons__ from the menu. The Administration page will display.
 3. Choose the __Manage add-ons__ option.
 4. Scroll to the page's bottom and click the __Settings__ link. The __Settings__ dialog will display. 
 5. Make sure the "Private listings" option is checked and click __Apply__.
 6. Scroll to the top of the page and click the __Upload Add-on__ link.
 7. Enter the URL to the hosted location of your plugin descriptor. In this example, the URL is similar to the following:  http://localhost:8000/atlassian-plugin.xml. (If you are installing to an OnDemand instance, the URL must be served from the Marketplace, and will look like https://marketplace.atlassian.com/download/plugins/com.example.add-on/version/39/descriptor?access-token=9ad5037b)
 8. Press __Upload__. The system takes a moment to upload and register your plugin. It displays the __Installed and ready to go__ dialog when installation is complete. <img width="100%" src="../assets/images/installsuccess.jpeg" />
 9. Click __Close__.
 10. Verify that your plugin appears in the list of __User installed add-ons__. For example, if you used Hello World for your plugin name, that will appears in the list.
