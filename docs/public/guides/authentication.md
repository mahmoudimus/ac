# Authentication

Atlassian Connect employs usage of a technology called [JWT (JSON Web Token)](http://self-issued.info/docs/draft-ietf-oauth-json-web-token.html)
 to authenticate add-ons.

## Implementation Examples

Most modern languages have JWT libraries available. Prior to implementing JWT, consider using an existing library.

* Java - [atlassian-jwt](https://bitbucket.org/atlassian/atlassian-jwt/)
* Node.js - [node-jwt-simple](https://github.com/hokaccha/node-jwt-simple)
* Ruby - [ruby-jwt](https://github.com/progrium/ruby-jwt)


## Handling JWT requests

Atlassian Connect add-ons specify that they are using the JWT authentication mechanism in the `authentication` module.
An add-on must accompany this with the `lifecycle` module to enable the add-on to handle the authentication process.

```
{
    "baseUrl": "http://localhost:3000",
    "key": "atlassian-connect-addon",
    "authentication": {
        "type": "jwt"
    },
    "lifecycle": {
        "installed": "/add-on-installed-callback"
    }
    modules: {} // etc
}
```

The `lifecycle:installed` property is a callback url which is called when an add-on is installed into an Atlassian
application.
