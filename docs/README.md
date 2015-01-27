# How to Doco Atlassian Connect

This directory contains everything you need to generate documentation for Atlassian Connect. First, what does the Atlassian Connect documentation cover?

* Getting started guide -- `public/guides/index.md`
* Other potential guides -- `public/guides/*.md`
* Module reference docs driven by JavaDoc in ModuleBeans -- these are auto generated using the JSON schema and lives in `public/modules/*`
* Any other documentation that we can think of (e.g., FAQs, Help & Support, etc.)

## How does it all work?

We use a Nodejs based static site generator called [Harp](http://harpjs.com/). It's a very simple tool that acts as a server and generator. When you're writing docs, it's nice to be able to see what it will actually look like. That's what the server is for. When we're ready to publish the docs we can generate static HTML files and publish them to DAC.

### Directory structure

    .
    ├── README.md (this file)
    ├── build.js (extracts JSON schema and updates harp.json with schema contents)
    ├── harp.json (acts as a global JSON that's available to the templates. Contains JSON schema used for modules docs)
    ├── package.json (standard Nodejs package.json)
    └── public
        ├── _layout.ejs (global layout)
        ├── _partials
        │   └── _sidebar.ejs
        ├── assets
        │   ├── css
        │   │   └── styles.less
        │   ├── images
        │   └── js
        ├── modules
        │   ├── confluence (build.js will populate this with modules markdown files)
        │   ├── index.md
        │   └── jira (build.js will populate this with modules markdown files)
        ├── guides
        │   └── index.md (getting started guide)
        └── index.md (homepage)

### About Harp

Harp takes everything stored under the `public` directory and preprocesses it. Harp supports Jade, Markdown, EJS, CoffeeScript, LESS, and Stylus. If you want to write plain ole HTML, CSS, JS, you can do that too. However, to adhere to a common convention, let's assume the following rules:

* All guides must be written with Markdown
* All modules JavaDocs must be written with Markdown
* Any unconventional styling you need to apply to your doco can be done with plain ole HTML within Markdown

Got it? If not, all you need to know is that you need to write your docs in Markdown!

## Workflow for generating documentation

To generate these docs, you'll need to make sure you have Nodejs installed. If you're on a mac, I suggest just doing a `brew install node`. Once you've got Nodejs installed, just run the following to install all the dependencies:

    npm install

### Running the server

Harp has it's own server that allows you to preview your doc dynamically without having to rebuild it after each change. The server will pick up changes to your Markdown and any changes to your templates or assets -- the Harp preprocessor is dynamic.

It's nice that Harp picks up changes dynamically, but it doesn't automatically update the browser when these changes are detected. For that, I recommend the awesome [LiveReload](http://livereload.com/) mac app. Harp + LiveReload makes it possible to work on your doc and see your changes in real-time.

To run the server, simply issue the following command:

    npm run-script start

This will launch an HTTP server on <http://localhost:9000>.

### Generating static docs

You'll likely never need to do this locally since we'll probably be doing this during the builds, but if you do, it's simply:

    npm run-script build

## A word about the JSON schema

As awesome as Doklovic is, his JSON schema doesn't get generated dynamically. Unfortunately, it still requires Maven to do the heavy lifting. So, today, to generate the JSON schema (if you make changes to the ModuleBeans JavaDocs), you have to:

    mvn install -DskipTests
    
or just to generate the schema:

	mvn process-classes

After you do that, you'll need to run:

    npm run-script update

This will pick up the updated JSON schema and update `harp.json` with the new schema.

## Doc redirects

See https://extranet.atlassian.com/x/aYk3j
