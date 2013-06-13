# Atlassian Remotable Plugins

This is the core repository behind [Atlassian Connect](https://developer.atlassian.com/display/AC/).

When getting started developing within Atlassian Remotable Plugins, these commands will come in handy:

You may need to run a `mvn install` from the project directory first before running these 
commands in the `/plugin` directory. Make sure to use Maven 2.1.0, the version that ships with
and is supported by the Atlassian Plugin SDK.

## Starting the plugin

Choose one of the following, for starting the plugin in the refapp, confluence or jira

    mvn amps:debug
    mvn amps:debug -Dproduct=confluence
    mvn amps:debug -Dproduct=jira
    
## Deployments

Deploy the plugin at runtime in the desired product:

    mvn amps:cli
    mvn amps:cli -Dproduct=confluence
    mvn amps:cli -Dproduct=jira

Deploy the test plugin (code in `src/test/resources`) with the cli `tpi` for test plugin installation, works like the usual `pi`

Test your changes in all three products at once - open up three terminals and execute the above `amps:debug` commands, one per product.  You will also need three more tabs for each of the `cli` invocations.
