# Getting Started

## Add-on Descriptor

## Installing your Add-on

### Install flow
<div class="diagram">
participant Admin
participant Add_on_server
participant OnDemand
Admin->OnDemand:Install add-on descriptor through UPM
OnDemand->Add_on_server:info about OnDemand instance\nincluding shared keys
OnDemand->Admin:Notified of successful installation 
</div>

### Render flow
<div class="diagram">
participant User
participant Browser
participant Add_on_server
participant OnDemand
User->OnDemand: View your Hello World page
OnDemand->Browser:OnDemand sends back page\nwith iframe to your addon 
Browser->Add_on_server:GET /helloworld.html?signed_request=* 
Add_on_server->Browser:Responds with contents of\n helloworld.html page 
Browser->User:Requested page\nrendered
</div>

## Developing and Testing your Add-on

### Local development loop

### Testing in OnDemand

### Deploying

## Securing your Add-on

### Identifying Users

### Using OAuth and JWT

### Scopes