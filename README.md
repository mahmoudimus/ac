# Atlassian Connect

[Atlassian Connect](https://connect.atlassian.com) is a platform for developers to build add-ons to integrate with
Atlassian’s cloud offerings. An add-on could be an integration with another existing service, new features for the
Atlassian application, or even a new product that runs within the Atlassian application.

This repository contains `atlassian-connect-plugin`, the implementation of Atlassian Connect for a subset of the
products based on [`atlassian-plugins`](https://bitbucket.org/atlassian/atlassian-plugins): JIRA and Confluence.

## Dependencies

* JDK 7
* Maven 3.2

## Usage

To build and run an Atlassian product with the development version of Atlassian Connect:

    mvn clean install
    mvn -pl plugin amps:debug -Dproduct=<jira|confluence>

To run an Atlassian product with a recent release of Atlassian Connect, see
[Release notes](https://developer.atlassian.com/static/connect/docs/latest/resources/release-notes.html) in the
developer documentation.

To run with Universal Plugin Manager able to connect to Atlassian Marketplace, append the following parameter to the command.

    -Djvmargs='-Datlassian.upm.on.demand=true'

## Reporting a problem

See [Getting help](https://developer.atlassian.com/static/connect/docs/latest/resources/getting-help.html) in the
developer documentation or raise an issue in the [AC](https://ecosystem.atlassian.net/browse/AC) project
on Atlassian Ecosystem JIRA.

## Development guide

### Contributions

Contributions are encouraged!

1. Create an issue in either of the following Atlassian Ecosystem JIRA projects.
    * [AC](https://ecosystem.atlassian.net/browse/AC) (Atlassian Connect)
    * [ACJIRA](https://ecosystem.atlassian.net/browse/ACJIRA) (JIRA Ecosystem)
    * [CE](https://ecosystem.atlassian.net/browse/CE) (Confluence Ecosystem)
2. If you are an Atlassian developer, follow the [internal developer's guide](https://extranet.atlassian.com/display/ARA/Atlassian+Connect+Internal+Developer%27s+Guide)
3. Create your feature branch, e.g. `feature/AC-1-create-project`
    * The prefix `feature/` is required for branch builds to run (without passing builds, you cannot merge your pull request)
    * Include your issue key and a short description
4. Push your changes, prefixing each commit message with the issue key
5. Create a pull request against this repository

### Repository structure

* `api-parent` - the parent of all modules containing public interfaces
	* `api` - a draft application programming interface for the plugin
	* `spi` - a draft service provider interface for the plugin
* `bin` - utility scripts
* `confluence` - the parent of all Confluence-specific modules
	* `confluence-support` - support for Atlassian Connect in Confluence
	* `confluence-reference-plugin` - a reference implementation of some SPI interfaces for Confluence
* `crowd-support` - support for Atlassian Connect in products that use Atlassian Crowd
* `docs` - a Node.js project for generating [the developer documentation](https://connect.atlassian.com)
* `integration-tests` - integration tests for the plugin
* `jira` - the parent of all JIRA-specific modules
	* `jira-reference-plugin` - a reference implementation of some SPI interfaces for JIRA
* `modules` - bean representations of add-on JSON descriptor elements
* `plugin` - groups the other modules and [`atlassian-connect-js`](https://bitbucket.org/atlassian/atlassian-connect-js) into a plugin
* `plugin-lifecycle-tests` - wired tests for the plugin lifecycle, requiring plugin uninstallation
* `test-support-plugin` - a collection of utility classes for unit and integration testing
* `wired-tests` - wired tests for the plugin

### Branches

This repository uses the [git flow](https://www.atlassian.com/git/workflows#!workflow-gitflow) branching workflow.

* `master` - contains the latest release
* `develop` - contains the stable development version

### Building

To build the plugin:

    mvn clean install

### Running tests

To run unit tests:

    mvn test

To run JavaScript unit tests:

    mvn clean package -Pkarma-tests -DskipUnits

To run wired tests:

    mvn clean install
    mvn -pl wired-tests verify -Pwired

To run plug-in lifecycle tests:

    mvn clean install
    mvn -pl plugin-lifecycle-tests verify -PpluginLifecycle

To run integration tests:

    mvn clean install
    mvn -pl integration-tests verify -Pit [-DtestGroups=...]

To run add-on descriptor validation tests:

    mvn clean install
    (cd bin/marketplace; npm install)
    node bin/marketplace/validate-descriptors.js --type json --testReport=plugin/src/test/resources/descriptor/descriptor-validation-results.json
    mvn -pl plugin test -DdescriptorValidation=true

### Updating developer documentation

To generate [the developer documentation](https://connect.atlassian.com):

    mvn clean install site

Also see the README in the [`docs`](docs) directory.

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE.txt).