# Atlassian Connect

This is the core repository behind [Atlassian Connect](https://developer.atlassian.com/display/AC/).

When getting started developing within Atlassian Connect, these commands will come in handy:

## Prerequisites

* Maven 3 (n.b. the Atlassian SDK currently ships with Maven 2.1)

## Development

The Atlassian Connect team uses [git flow](https://www.atlassian.com/git/workflows#!workflow-gitflow).

The `master` branch points to the latest `atlassian-connect` release. If you are looking for bleeding edge,
you probably want to be on the `develop` branch.

## Javascript

Most of the connect javascript is now part of it's [own project](https://stash.atlassian.com/projects/AC/repos/atlassian-connect-js/) - check there for additional instructions. The project is consumed using the package.json file in the plugin directory.

### Contributions

Contributions are encouraged! To start working on Atlassian Connect, follow this guide:

1. Ensure there is a relevant JIRA issue in project [AC](https://ecosystem.atlassian.net/browse/AC)
2. Run `mvn jgitflow:feature-start`
3. Name your feature branch with your issue key and short description. e.g. `AC-1-implement-macro-editor`
4. Commit and push
5. Create a pull request in [Stash](https://stash.atlassian.com/projects/AC/repos/atlassian-connect/) with 1-3 reviewers from the team (depending on the complexity of the change).

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

Wired tests can be run from [the Plugin Test Console](https://developer.atlassian.com/display/DOCS/Run+Wired+Tests+with+the+Plugin+Test+Console) inside the host application.
They can also be run directly with Maven or IDEA, but that requires setting the `baseUrl` parameter to point the test runner to your local instance, e.g. `-Dbaseurl=http://localhost:2990/jira`.

To debug a test, you must first [create a remote debug target](https://developer.atlassian.com/docs/developer-tools/working-in-an-ide/creating-a-remote-debug-target).
Then, start debugging using the remote debug target and thereafter run the test in debug mode.

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

## Reloading

The atlassian connect plugin uses [quickreload](https://extranet.atlassian.com/pages/viewpage.action?pageId=2227343457).
It will automatically reload the connect plugin as soon as it detects that the jar has changed. The recommended fastest way to make this happen
is to keep `atlas-cli` running in the `plugin` directory, and enter the `package` command to re-build.

If you want quickreload to watch and auto-load other plugins from source, add the directories to the `.quickrelaod` file in the source tree root.

quickreload also automatically finds and uses the `resources` directory of our plugin
(and any plugin it's watching that has the standard maven layout), so there is no need to set `-Dplugin.resource.directories` in `MAVEN_OPTS` anymore.

To load via the Atlassian SDK, use

    mvn amps:cli -pl plugin -Dproduct=jira

Where `<product>` is either `jira` or `confluence`. If left empty, the plugin will run inside of JIRA

To load via curl:

TODO: add new upm install instructions here.
