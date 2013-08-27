# Atlassian Connect

This is the core repository behind [Atlassian Connect](https://developer.atlassian.com/display/AC/).

When getting started developing within Atlassian Connect, these commands will come in handy:

## Prerequisites

* Maven 3 (n.b. the Atlassian SDK currently ships with Maven 2.1)

## Building

To build the plugin, run:
  
    mvn clean install -DskipTests=true

## Testing Locally

To run the integration tests locally, *cd into the integration-tests directory*
  
    mvn clean verify -DtestGroups=jira

or

    mvn clean verify -DtestGroups=confluence

To run a single test/method, do something like:

    mvn clean verify -DtestGroups=jira -Dit.test=TestPageModules#testMyGeneralLoaded

## Running

To run an Atlassian product with the development version of Atlassian Connect:

    mvn amps:debug -pl plugin -Dproduct=<product>

To run with UPM available to connect to the marketplace:

    mvn amps:debug -pl plugin -Dproduct=<product> -Dproduct.version=<version> -Djvmargs='-Datlassian.upm.on.demand=true'

eg,

    mvn amps:debug -pl plugin -Dproduct=jira -Dproduct.version=6.1-for-AC-2 -Djvmargs='-Datlassian.upm.on.demand=true'

To run with a fast JS dev loop, set MAVEN_OPTS such that it contains the `plugin/src/main/resources` directory:

    MAVEN_OPTS="$MAVEN_OPTS -Dplugin.resource.directories=/Users/Me/dev/atlassian-connect/plugin/src/main/resources"

To reload plugin (pi) within an Atlassian product:

    mvn amps:cli -pl plugin -Dproduct=jira

Where `<product>` is either `jira` or `confluence`. If left empty, the plugin will run inside of JIRA

To load via curl:

TODO: add new upm install instructions here.