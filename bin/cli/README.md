## Introduction

This is an experimental installation and command execution system for the p3 plugin development tool.

For general Plugins 3 development, run the following command to install the p3 CLI tool:

	  curl https://bitbucket.org/atlassian/remotable-plugins/raw/master/bin/cli/p3.sh | sh

If you already have the (Atlassian Plugin SDK)[https://developer.atlassian.com/display/DOCS/Atlassian+Plugin+SDK+Documentation],
and its bin directory is installed on your path, then p3 will use the existing SDK.  Otherwise, it will download and install
the SDK for its own use in it's home directory (~/.p3).

If you're a developer working on the remotable-plugins itself, then you'll also want to export the RP_HOME environment
variable to point to your local git repository directory for the remotable-plugins project.  If you don't have RP_HOME set,
then installation of the p3 scripts will also install its own working version of the remoteaps-plugin project in it's home directory.

When setting RP_HOME to an existing remotable-plugins repository, you can install without network access like so:

	  cat remotable-plugins/bin/cli/p3.sh | sh

## Command Examples

Run a refapp instance:

    p3 run

Run a JIRA instance in debug mode (debugger port 5005), using shorthand:

    p3 r -d jira

Create a new JavaScript-based plugin with key 'myapp':

    p3 create myapp

Create a new CoffeeScript plugin with the minimal template and oauth support with key, name, and description, using shorthand:

    p3 c -cmo myapp "My App" "My kickass app!"

Start the plugin in subdirectory 'myapp':

    p3 start myapp

Start the plugin in subdirectory 'myapp' in debug mode (debugger port 5004), using shorthand and rebuilding the container first:

    p3 s -rd myapp

Display p3's current environment settings:

    p3 env

Rebuild remotable-plugins:

    p3 rebuild

Rebuild remotable-plugins cleanly while also running tests and toggling online mode, using shorthand:

    p3 rb -cto

Rebuild just the container module, using shorthand:

    p3 rb container

Update remotable-plugins from the repository (only if not overriding $RP_HOME):

    p3 update

Uninstall remotable-plugins and all of its working files (only if not overriding $RP_HOME):

    p3 uninstall
