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

    mvn amps:debug -pl integration-tests -Dproduct=<product>
    
Where `<product>` is either `jira` or `confluence`. If left empty, the plugin will run inside of the [Atlassian RefApp](https://developer.atlassian.com/display/DOCS/About+the+Atlassian+RefApp).
