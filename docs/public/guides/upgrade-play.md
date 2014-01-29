# Upgrade atlassian-connect-play-java

If you are creating a new `atlassian-connect-play-java` (AC Play) add-on, then all you need to do is follow the instructions in the
[readme of AC Play](https://bitbucket.org/atlassian/atlassian-connect-play-java/).

<div class="aui-message info">
    <span class="aui-icon icon-info"></span>
    Upgrading to AC Play 0.7.x will replace OAuth 1 with JWT.
</div>

If you have an existing Connect add-on using AC Play `0.6.x`, follow these steps to update.

### Upgrade AC Play
In your `project/Build.scala` file, change the AC Play dependency to
```
"com.atlassian.connect" % "ac-play-java_2.10" % "0.7.0-BETA7"
```

### Play 2.2

In version `0.6.4` of AC Play was upgraded to version 2.2. The following changes are required:

1. edit `project/build.properties`. Change the contents to
```
sbt.version=0.13.0
```
2. If you have used JsonNode and related classes then you'll need to make the following updates as Play has changed the implementation that it uses

    a. Change all the imports to
```
import com.fasterxml.jackson.databind.*;
```
    b. Drop the "get" from all the property access. e.g.
```
jsonNode.getFieldNames() -> jsonNode.fieldNames()
```
3. Consult the [Play 2.2 Migration Guide](http://www.playframework.com/documentation/2.2.0/Migration22)

### JSON Descriptor

1. Rename `atlassian-plugin.xml` to `atlassian-connect.json` per our [migration guide](./migrating-from-xml-to-json-descriptor.html)

    a. make sure you remove any `&amp;` characters from the url as it is required in xml but breaks in json
    b. Permissions need to be migrated to [scopes](../concepts/scopes.html)
    c. You _must_ have the lifecycle `installed` event registered to `/installed`

2. Update your `Application` class to invoke the `ACController`'s json descriptor method. The end result should look something like:

<pre><code data-lang="java">
private static Supplier<Result> descriptor()
{
    return new Supplier<Result>()
    {
        @Override
        public Result get()
        {
            return AcController.descriptor();
        }
    };
}
</code></pre>

### JWT

The JWT authentication mechanism is covered in detail in the [authentication documentation](../concepts/authentication.html).

1. Make sure that you specify `jwt` as the authentication mechanism in your descriptor. e.g.
```
"authentication": {
        "type": "jwt"
}
```
2. add a new database evolution file with the following contents
```
alter table ac_host add column sharedSecret varchar(512);
```
3. Anywhere that you have used `@CheckValidOAuthRequest` change it to `@AuthenticateJwtRequest`.
<pre><code data-lang="java">
@AuthenticateJwtRequest
public Result index() throws Exception
{ ... }
</code></pre>

### Troubleshooting

To simplify debugging we recommend that you increase the logging levels of the atlassian connect modules.

```
logger.ac=TRACE
```

Similarly it is recommended to increase the logging level of the jwt components in the host product. e.g.

```
logger.com.atlassian.jwt=TRACE
```


