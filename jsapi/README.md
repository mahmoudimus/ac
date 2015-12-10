# Atlassian Connect JavaScript API

This Node.js project builds the JavaScript API for Atlassian Connect based on
[`atlassian-connect-js`](https://bitbucket.org/atlassian/atlassian-connect-js).

## Development guide

### Using a local clone of `atlassian-connect-js`

To override the version of `atlassian-connect-js` with that of a local clone, include the following parameter when
building the project, where the path provided can be an absolute path or a relative path from this directory,
typically `../../atlassian-connect-js`.

    -Datlassian.connect-js.path=<path>
