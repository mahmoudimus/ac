# Atlassian Connect JavaScript API

This Node.js project builds the JavaScript API for Atlassian Connect based on
[`atlassian-connect-js`](https://bitbucket.org/atlassian/atlassian-connect-js/src/62eb630652c3008cc916971d4b386b270c0202b5?at=release/3.0.0-do-not-delete).

## Development guide

### Using a local clone of `atlassian-connect-js`

To override the version of `atlassian-connect-js` with that of a local clone, include the following parameter when
building the project, where the path provided can be an absolute path or a relative path from this directory,
typically `../../atlassian-connect-js`.

    -Datlassian.connect-js.path=<path>

If you want local changes to `atlassian-connect-js` to be automatically loaded into a running JIRA or Confluence instance,
follow the instructions in the [`atlassian-connect-js` README](https://stash.atlassian.com/projects/AC/repos/atlassian-connect-js/browse/README.md?at=refs%2Fheads%2Frelease%2F3.0.0-do-not-delete).