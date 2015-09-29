# Atlassian Connect Developer Documentation

This Node.js project produces the developer documentation for Atlassian Connect, which is publicly available at
[https://connect.atlassian.com](https://connect.atlassian.com).

The documentation is generated using [Harp](http://harpjs.com), a static web server with built-in preprocessing.

## Development guide

### Generating the documentation

From the repository root, to build the project and set up dependencies for documentation generation:

    mvn clean install pre-site -DskipTests

To regenerate the documentation and start a web server at http://localhost:9000 with automatic change detection:

    cd docs
    npm i
    npm run-script start

To regenerate static documentation:

    cd docs
    npm i
    npm run-script build

If you are in the `docs` directory and your npm commands result in `Error: ``libsass`` bindings not found. Try reinstalling ``node-sass``?` then run:

    npm rebuild node-sass

### Updating the documentation

#### Descriptor modules

For validation and documentation of add-on descriptors, JSON schemas are generated from the bean representations of
add-on JSON descriptor elements in `modules`. As part of documentation generation, these schemas are
copied from `plugin` and used to generate a documentation page for each descriptor element.

Documentation for descriptor beans must be valid Javadoc. If you need more control over formatting and style, use HTML
within Javadoc. Do not use Markdown within Javadoc.

JSON schemas are generated in a two-step process using the [`json-schemagen`](https://bitbucket.org/atlassian/json-schemagen)
Maven plugin.

* The `generate-support-docs` goal uses a custom Javadoc doclet to generate the files `jsonSchemaDocs.json` and `jsonSchemaInterfaces.json`
* The `generate-schema` goal uses those files to produce a JSON schema

Several schemas are generated from the descriptor beans: a shallow schema excluding the module list and a schema for
each product, including any specific modules for that product.

To regenerate the schemas after changing the source code or the Javadoc of `modules`:

	mvn -pl plugin process-classes

To explicitly invoke the `json-schemagen` goals to generate an individual schema (using [mvnvm](http://mvnvm.org) and
functionality introduced in Maven 3.3):

    mvn --mvn-version 3.3.3 -pl plugin external.atlassian.json:json-schemagen-maven-plugin:generate-support-docs@schema-support external.atlassian.json:json-schemagen-maven-plugin:generate-schema@<jira|confluence>-schema

#### JavaScript API modules

As part of documentation generation, the source code of the Atlassian Connect JavaScript API is copied from `plugin` and
used to generate a documentation page for each module with [JSDoc](http://usejsdoc.org).

#### REST and RPC API scopes

As part of documentation generation, the JSON files defining the required scope for each REST and RPC API method from
`plugin` and used to generate a documentation page for each product API.

#### Other pages

All manually managed documentation pages must be written using Markdown. If you need more control over formatting and
style, use HTML within Markdown.

### Known issues

The documentation generator depends on the `node-sass` library. The installation of that library is tied to your Node.js
version and OS version. You may run into node-sass binding issues if you run this command with a version of Node.js
greater than >= 0.12.0. To fix this issue, run the following command from `docs`.

    npm rebuild node-sass