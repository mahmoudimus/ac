# Atlassian Connect Service Provider Interface

This module contains interfaces that can be implemented to add product-specific functionality to the Atlassian Connect
platform. See the [`reference-plugin`](../reference-plugin) for an example implementation of each of the plugin 
extension points.


## Add-on Descriptor Module Type

To add a new module type, you'll need to implement three interfaces:

 * `BaseModuleBean` - the bean representation of an instance of your module type. Modules from the add-on descriptor 
   will be deserialized into this class. Your bean should have a required unique `String key` field and an 
   `I18nProperty name` for a human readable name. Other useful fields you might want to include are a
   `List<ConditionalBean> conditions` and a `Map<String, String> params`. You should add public documentation to this
   class as Javadoc, detailing how and why your module type should be used. See the [README of the docs module]
   (../../docs) for more information.
 * `ConnectModuleMeta` - provides metadata for your module type, including the key of your module type in the JSON descriptor,
   your `BaseModuleBean` implementation, and whether or not multiple modules of your type are allowed for a single add-on.
 * `ConnectModuleProvider` - handles deserialization and validation of modules of your type from the add-on descriptor, 
   and creates plugin module descriptors for those modules. Typically, implementations will extend
   AbstractConnectModuleProvider which implements the required module bean deserialization.
   
Then declare your module provider in your `atlassian-plugin.xml` using the `connect-module` plugin module, e.g.

    <connect-module key="my-module-provider" class="com.atlassian.plugin.MyModuleProvider"/>

Your module type should use [JSON schema](http://json-schema.org/) to validate the syntax of the descriptor element.
If you want to add a cross-product module, or a JIRA or Confluence-specific module, we recommend extending
`AbstractConnectCoreModuleProvider`, `AbstractJiraConnectModuleProvider`, or `AbstractConfluenceConnectModuleProvider`.
These classes provide schema validation for modules of your type.

The schemas used for validation and our public documentation are generated from `CommonModuleList`, `JiraModuleList`,
and `ConfluenceModuleList` respectively. You'll need to add your bean to one of those module list classes. Add your 
bean as a list if you allow multiple modules of your type to be declared in a single add-on, otherwise add
it as a single object. Annotate your bean using [JSON SchemaGen](https://bitbucket.org/atlassian/json-schemagen) 
annotations to specify how the schema for your module type should be generated, e.g. if a particular field is required.

Any new module type should be covered by integration tests. Your tests should install an add-on with modules of 
the module type, and test the expected functionality in the host product. Any extra validation you perform in your 
implementation of `ConnectModuleProvider` should be covered by unit or wired tests. You could also implement wired 
tests for your implementation of the `createPluginModuleDescriptors` method.


## API Scope Whitelist

API scope whitelists define the HTTP endpoints of the application that are available for add-ons to access. They
also define which [add-on scopes](https://developer.atlassian.com/static/connect/docs/latest/scopes/scopes.html) are 
required to access each endpoint. A whitelist should be provided as a JSON file, declared in your `atlassian-plugin.xml`, e.g.

    <connect-api-scope-whitelist key="my-api-scope-whitelist" resource="/scope/my-whitelist.json"/>

Each plugin should only need to declare a single whitelist file. Within that file, declare all endpoints that you wish
to make accessible in a list under the key `paths`, `restPaths`, `jsonRpcPaths`, or `xmlRpcPaths`, depending on the 
type of endpoint you're specifying. You should provide each endpoint object with a unique `key`, along with other 
information specific to the endpoint type. 

An object in the `paths` list should contain its own `paths` value containing regex that identifies the path or paths 
you want to make available, along with a `public` boolean value to specify if the path is public or not, e.g.

```
"paths": [
    {
        "key": "my-path",
        "paths": ["/my/path($|/.*)"],
        "public": true
    }
]
```

Objects in the `restPaths` list should also have a `public` value, along with a `name`, a list of `versions` of the API 
this endpoint is found in, and a `basePaths` value containing regex identifying the path or paths, e.g.

```
"restPaths": [
    {
        "key": "my-rest-path",
        "name": "my path",
        "basePaths": ["/my/path(/.*)"],
        "versions": ["2", "latest"],
        "public": true
    }
]
```

Objects in the `jsonRpcPaths` and `xmlRpcPaths` list should specify a list of `rpcMethods` to cover. `jsonRpcPaths` 
should also specify a `paths` list, while `xmlRpcPaths` should specify a list of `prefixes`, e.g.

```
"jsonRpcPaths": [
    {
        "key": "my-json-rpc-path",
        "paths": ["/my-path-v1", "/my-path-v2"],
        "rpcMethods": [
            "method1",
            "method2"
        ]
    }
],
"xmlRpcPaths": [
    {
        "key": "my-xml-rpc-path",
        "prefixes": ["prefix1", "prefix2"],
        "rpcMethods": [
            "method1",
            "method2"
        ]
    }
]
```

Then define a list of `scopes` object. Each object in this list will match an add-on scope with the paths you just 
defined. Provide a `key` for each object that matches an add-on scope, a list of `pathKeys`, `restPathKeys`, 
`jsonRpcPathKeys` and/or `xmlRpcPathKeys` that match the keys you defined earlier, and a list of `methods` that this 
add-on scope should provide access to, e.g.

```
"scopes": [
    {
        "key": "READ",
        "restPathKeys": ["my-rest-path"],
        "pathKeys": ["my-path"],
        "methods": ["GET"]
    }
]
```

For an example implementation of a scope whitelist, see [`test-whitelist.json`]
(../reference-plugin/src/main/resources/scope/test-whitelist.json).

If you add to the JIRA, JIRA Agile, Confluence, or common whitelist files, the changes will appear on the corresponding
page in the documentation when it gets built.


## Condition Class Resolver

A condition class resolver is responsible for mapping [condition keys]
(https://developer.atlassian.com/static/connect/docs/latest/concepts/conditions.html) in an add-on descriptor with a 
condition class. Each plugin should only need to declare a single condition class resolver in `atlassian-plugin.xml`, e.g.

    <connect-condition-class-resolver key="my-condition-class-resolver" class="com.atlassian.plugin.MyConditionClassResolver"/>

Your condition class resolver class should implement the `ConnectConditionClassResolver` interface. The simplest 
implementation will just implement the `getEntries` method, returning a list of `Entry`s that map from a descriptor
condition key to an implementation of a plugin `Condition`. Entries can be created using the provided builder.

`Condition`s are given a context map when the `shouldDisplay` method is called. Depending on how conditions are 
specified in an add-on descriptor, the Connect plugin may attempt to evaluate the condition when the context map is 
not fully populated. If the `Condition` that you're mapping to does not access any fields of the context map when it
performs its evaluation, mark the entry as `contextFree` using the entry builder. This flag tells the Connect plugin 
that it is safe to resolve to this condition class even when the full host application context is not available.

Entries can also be provided with predicates that must evaluate to true for the entry to be used. Predicates are 
evaluated against any parameters that are provided with the condition in the add-on descriptor. For example, a 
condition with the key `entity_property_equal_to` will only resolve to the `AddonEntityPropertyEqualToCondition` 
class if a parameter is defined with the condition in the descriptor with the key `entity` and the value `addon`.

For a simple example implementation, see [`ReferenceConditionClassResolver`]
(../reference-plugin/src/main/java/com/atlassian/plugin/connect/reference/ReferenceConditionClassResolver).

Note that conditions are generally autowired in the context of the plugin that registers a plugin module making use 
of it. For any new condition added, please include wired or integration tests that cover this autowiring.


## Web Fragment Location Blacklist

By default, any web fragment location can be used by Atlassian Connect add-ons. To restrict add-on modules from 
specifying particular web fragment locations, add a `connect-web-fragment-location-blacklist` module to your 
`atlassian-plugin.xml` file. The blacklist is defined entirely within the xml, and can contain multiple blacklisted 
locations for both web items and web panels, e.g.

```
 <connect-web-fragment-location-blacklist key="my-blacklist">
     <web-panel-locations>
         <location>atl.header.webpanel.blacklisted</location>
     </web-panel-locations>
     <web-item-locations>
         <location>atl.header.webitem.blacklisted</location>
         <location>atl.header.webitem.also-blacklisted</location>
     </web-item-locations>
 </connect-web-fragment-location-blacklist>
```


## Context Parameter Mapper

To add a new [context parameter](https://developer.atlassian.com/static/connect/docs/latest/concepts/context-parameters.html)
that can be requested from an add-on web fragment module, implement the `ConnectContextParameterMapper` interface.
Your implementation will be given the context of an add-on web fragment, and will be responsible for extracting a 
parameter value to be provided to that web fragment. Declare your context parameter mapper in your `atlassian-plugin.xml`, e.g.

    <connect-context-parameter-mapper key="my-context-parameter-mapper" class="com.atlassian.plugin.MyContextParameterMapper"/>


