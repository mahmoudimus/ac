# Atlassian Connect

[Atlassian Connect](https://connect.atlassian.com) is a platform for developers to build add-ons to integrate with
Atlassian’s cloud offerings. An add-on could be an integration with another existing service, new features for the
Atlassian application, or even a new product that runs within the Atlassian application.

This repository contains `atlassian-connect-plugin`, the implementation of Atlassian Connect for a subset of the
products based on [`atlassian-plugins`](https://bitbucket.org/atlassian/atlassian-plugins): JIRA and Confluence.

**NOTE:** Atlassian developers, see the [internal developer's guide](https://extranet.atlassian.com/display/ECO/Atlassian+Connect+-+Internal+Developer%27s+Guide) for more details.

## Dependencies

* JDK 8
* Maven 3.3.9

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

1. Create an issue in one of the following Atlassian Ecosystem JIRA projects.
    * [AC](https://ecosystem.atlassian.net/browse/AC) (Atlassian Connect)
    * [ACJIRA](https://ecosystem.atlassian.net/browse/ACJIRA) (JIRA Ecosystem)
    * [CE](https://ecosystem.atlassian.net/browse/CE) (Confluence Ecosystem)
2. Create your feature branch, e.g. `feature/AC-1-create-project`
    * The prefix `feature/` is required for branch builds to run (without passing builds, you cannot merge your pull request)
    * Include your issue key and a short description
3. Push your changes, prefixing each commit message with the issue key
4. Create a pull request against this repository

### Repository structure

* `bin` - utility scripts
* `components` - the shared components of the plugin
    * `api` - a draft application programming interface for the plugin
    * `core` - the core cross-product implementation
    * `core-extensions` - cross-product extensions for web fragments, webhooks etc.
    * [`extension-spi`](components/extension-spi) - a draft service provider interface defining components that the
    host application or plugin can provide in order to extend Connect
    * `host-spi` - a draft service provider interface defining components that a host application needs to provide in
    order to support Connect
    * `modules` - bean representations of add-on JSON descriptor elements
    * `reference-plugin` - a cross-product reference implementation of some SPI interfaces
* `confluence` - the parent of all Confluence-specific modules
    * `confluence-integration-tests` - Confluence-specific integration tests for the plugin
    * `confluence-reference-plugin` - a reference implementation of some SPI interfaces for Confluence
    * `confluence-support` - support for Atlassian Connect in Confluence
* `crowd-support` - support for Atlassian Connect in products that use Atlassian Crowd
* [`docs`](docs) - a Node.js project for generating [the developer documentation](https://connect.atlassian.com)
* `jira` - the parent of all JIRA-specific modules
    * `jira-integration-tests` - JIRA-specific integration tests for the plugin
    * `jira-reference-plugin` - a reference implementation of some SPI interfaces for JIRA
    * `jira-support` - support for Atlassian Connect in JIRA
* [`jsapi`](jsapi) - builds the JavaScript API based on [`atlassian-connect-js`](https://bitbucket.org/atlassian/atlassian-connect-js)
* `plugin` - groups the other modules into a plugin
* `tests` - the parent of all non-product-specific test modules
    * `core-integration-tests` - integration tests for the plugin's core functionality
    * `descriptor-validation-tests` - JSON schema validation of all public add-ons for JIRA and Confluence on Atlassian Marketplace
    * `integration-tests-support` - classes and utilities useful to both core and product-specific integration test modules
    * `marketplace-support` - utilities for working with Atlassian Marketplace in tests
    * `plugin-lifecycle-tests` - wired tests for the plugin lifecycle, requiring plugin uninstallation
    * `test-support-plugin` - a collection of test utility classes
    * `wired-tests` - wired tests for the plugin

### Branches

This repository uses the [git flow](https://www.atlassian.com/git/workflows#!workflow-gitflow) branching workflow.

* `master` - contains the latest release
* `develop` - contains the stable development version

### Building

To build the plugin:

    mvn clean install

To speed up subsequent builds, the `-` prefix can be used with the `-pl` option to exclude specific modules,
e.g. the `jsapi` module which invokes a time-consuming Node.js build.

    mvn -pl -jsapi clean install

Conversely, once the project has been built, it can be rebuilt with changes only from specific modules:

    mvn -pl jsapi,plugin clean install

### Extending the platform

Most functionality in Atlassian Connect is provided through the service provider interface for extensions.

See the README in the [`extension-spi`](components/extension-spi) for more details.

### Working with the JavaScript API

This repository contains extensions to the cross-product functionality provided by [`atlassian-connect-js`](https://bitbucket.org/atlassian/atlassian-connect-js).

See the README in the [`jsapi`](jsapi) directory for more details.

### Updating developer documentation

To generate [the developer documentation](https://connect.atlassian.com):

    mvn clean install site

See the README in the [`docs`](docs) directory for more details.

### Running tests

#### Unit tests

To run unit tests:

    mvn test

To run JavaScript unit tests:

    mvn clean package -Pkarma-tests -DskipUnits

#### Integration tests

Before running integration tests, build the plugin.

To speed up local development, all integration test modules are excluded by default. For these modules to be included
in the build, such as when running `mvn clean` or `mvn verify`, a specific profile must be activated manually. See the
commands below for the name of each profile.

To run wired tests:

    mvn -pl tests/wired-tests verify -Pwired -DskipITs=false

To run plug-in lifecycle tests:

    mvn -pl tests/plugin-lifecycle-tests verify -PpluginLifecycle -DskipITs=false

To run core integration tests:

    mvn -pl tests/core-integration-tests verify -Pit [-DtestGroups=...] -DskipITs=false 

To run JIRA integration tests:

    mvn -pl jira/jira-integration-tests verify -Pit [-DtestGroups=...] -DskipITs=false 
    
To run Confluence integration tests:

    mvn -pl confluence/confluence-integration-tests verify -Pit [-DtestGroups=...] -DskipITs=false 

To run add-on descriptor validation tests:

    mvn -pl tests/descriptor-validation-tests verify -PdescriptorValidation -DskipTests -DskipITs=false

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE.txt).
