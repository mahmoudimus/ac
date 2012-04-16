Atlassian Remote Apps Plugin is a plugin that supports a type of plugin called a Remote App.

https://remoteapps.jira.com/wiki/display/ARA

When getting started developing the Atlassian Remote Apps plugin, these commands will come in handy.
You may need to run a 'mvn install' from the project directory first before running these 
commands in the '/plugin' directory.

* Start the plugin in the desired product:
  Refapp: mvn amps:debug
  Confluence: mvn amps:debug -Dproduct=confluence
  JIRA: mvn amps:debug -Dproduct=jira

* Deploy the plugin at runtime in the desired product:
  Refapp: mvn amps:cli
  Confluence: mvn amps:cli -Dproduct=confluence
  JIRA: mvn amps:cli -Dproduct=jira

* Deploy the test plugin (code in src/test/resources) with the cli
  'tpi' for test plugin installation, works like the usual 'pi'

* Test your changes in all three products at once - open up three terminals and execute the above amps:debug commands,
  one per product.  You will also need three more tabs for each of the 'cli' invocations.

If committing to the gatekeeper build server, run 'bin/committer-setup.sh' first.
