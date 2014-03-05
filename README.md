# Atlassian Connect

This is the core repository behind [Atlassian Connect](https://developer.atlassian.com/display/AC/).

When getting started developing within Atlassian Connect, these commands will come in handy:

## Prerequisites

* Maven 3 (n.b. the Atlassian SDK currently ships with Maven 2.1)

## Development

The Atlassian Connect team uses [git flow](https://www.atlassian.com/git/workflows#!workflow-gitflow).

The `master` branch points to the latest `atlassian-connect` release. If you are looking for bleeding edge,
you probably want to be on the `develop` branch.

### Contributions

Contributions are encouraged! To start working on Atlassian Connect, follow this guide:

1. Ensure there is a relevant JIRA issue in project [AC](https://ecosystem.atlassian.net/browse/AC)
2. Run `mvn jgitflow:feature-start`
3. Name your feature branch with your issue key and short description. e.g. `AC-1-implement-macro-editor`
4. Commit and push
5. Create a pull request in [Stash](https://stash.atlassian.com/projects/AC/repos/atlassian-connect/)

For more details see the [internal developer's guide](https://extranet.atlassian.com/x/cAhDg).

## Building

To build the plugin, run:
  
    mvn clean install -DskipTests=true

## Testing Locally

To run the integration tests locally, *cd into the integration-tests directory*
  
    mvn clean verify -P it -DtestGroups=jira

or

    mvn clean verify -P it -DtestGroups=confluence

To run a single test/method, do something like:

    mvn clean verify -P it -DtestGroups=jira -Dit.test=TestPageModules#testMyGeneralLoaded

To run an integration test against a particular product in IDEA. (Note only applies to tests that can run against more than one product)
    Edit configurations -> VM Options = -DtestedProduct=<product>    


### Wired Tests
See [doco for details](https://developer.atlassian.com/display/DOCS/Run+Wired+Tests+with+the+Plugin+Test+Console)

To run manually

    cd plugin
    mvn amps:debug -Pwired -Dproduct=<jira|confluence> -Dproduct.version=<version>
    

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
