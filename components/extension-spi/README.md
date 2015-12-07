# Atlassian Connect Service Provider Interface

This module contains interfaces that can be implemented to add product-specific functionality to the Atlassian Connect
platform. See the [`reference-plugin`](../reference-plugin) for an example implementation of each of the plugin 
extension points.


## Connect Module Types
To add a new Connect module type, you'll need to implement three interfaces:
 * `BaseModuleBean` - the bean representation of an instance of your module type. Modules from the add-on descriptor 
 will be deserialized into this class.
 * `ConnectModuleMeta` - provides metadata for your module type, including the key of your module type in the JSON descriptor,
   your BaseModuleBean implementation, and whether or not multiple modules of your type are allowed for a single add-on.
 * `ConnectModuleProvider` - handles deserialization of modules of your type from the add-on descriptor, and creates plugin
   module descriptors for those modules.
   
Then declare your module provider in your atlassian-plugin.xml using the `connect-module` plugin module, e.g.

```
<connect-module key="my-module-provider" class="com.atlassian.plugin.MyModuleProvider"/>
```

If you want to add a cross-product module, or a JIRA or Confluence-specific module, we recommend extending
`AbstractConnectCoreModuleProvider`, `AbstractJiraConnectModuleProvider`, or `AbstractConfluenceConnectModuleProvider`. 
These classes implement the required module bean deserialization, and also provide schema validation for modules of 
your type. The schemas used for validation are generated from `CommonModuleList`, `JiraModuleList` and
`ConfluenceModuleList` respectively. You'll need to add your bean to one of those module list classes. Add your bean 
as a list if you allow multiple modules of your type to be declared in a single add-on, otherwise add
it as a single object. Annotate your bean using [JSON SchemaGen](https://bitbucket.org/atlassian/json-schemagen) 
annotations to specify how the schema for your module type should be generated, e.g. if a particular field is required.

Our public documentation is also generated from the schemas. Add documentation to your module bean as Javadoc 
detailing how and why your module type should be used. See the [docs README](../../docs) for more information.

You should create wired tests for testing your implementation of ConnectModuleProvider. You should test your 
implementation of the `createPluginModuleDescriptors` method and any extra validation you perform in 
`deserializeAddonDescriptorModules`.

## API Scope Whitelists
API scope whitelists define the endpoints of a plugin that are available for Connect add-ons to access. They
also define which add-on scopes are required to access each endpoint. A whitelist should be provided as a JSON file,
declared in your atlassian-plugin.xml, e.g.

```
<connect-api-scope-whitelist key="my-api-scope-whitelist" resource="/scope/my-whitelist.json"/>
```

Each plugin should only need to declare a single whitelist file. Within that file, declare all endpoints that you wish
to make accessible in a list under the key `paths`, `restPaths`, `jsonRpcPaths`, or `xmlRpcPaths`. Then using the `scopes`
key, define which endpoints an add-on will be able to access with each Connect add-on scope. For an example implementation
of a scope whitelist, see [`test-whitelist.json`](../reference-plugin/src/main/resources/scope/test-whitelist.json).

If you add to the JIRA, JIRA Agile, Confluence, or common whitelist files, the changes will appear on the corresponding
page in the documentation when it gets built.

## Condition Class Resolvers
A condition class resolver is responsible for mapping condition keys in an add-on descriptor with a condition class.
Each plugin should only need to declare a single condition class resolver in atlassian-plugin.xml, e.g.

```
<connect-condition-class-resolver key="my-condition-class-resolver" class="com.atlassian.plugin.MyConditionClassResolver"/>
```

Your condition class resolver class should implement the `ConnectConditionClassResolver` interface. The simplest 
implementation will just implement the `getEntries` method, returning a list of `Entry`s that map from a descriptor
condition key to a condition class. Entries can be created using the provided builder.

Entries can also be provided with predicates that must evaluate to true for the entry to be used. Predicates are 
evaluated against any parameters that are provided with the condition in the add-on descriptor. For example, a 
condition with the key `entity_property_equal_to` will only resolve to the `AddonEntityPropertyEqualToCondition` 
class if a parameter is defined with the condition in the descriptor with the key `entity` and the value `addon`.

For a simple example implementation, see [ReferenceConditionClassResolver](../reference-plugin/src/main/java/ReferenceConditionClassResolver).

## Web Fragment Location Blacklist
To restrict Connect add-on modules from specifying particular web fragment locations, add a 
`connect-web-fragment-location-blacklist` module to your atlassian-plugin.xml file. The blacklist is defined entirely
within the xml, e.g.

```
 <connect-web-fragment-location-blacklist key="my-blacklist">
     <web-panel-locations>
         <location>atl.header.webpanel.blacklisted</location>
     </web-panel-locations>
     <web-item-locations>
         <location>atl.header.webitem.blacklisted</location>
     </web-item-locations>
 </connect-web-fragment-location-blacklist>
```

## Context Parameter Mapper
To add a new [Connect context parameter](https://developer.atlassian.com/static/connect/docs/latest/concepts/context-parameters.html)
that can be requested from an add-on web fragment module, implement the `ConnectContextParameterMapper` interface.
Your implementation will be given the context of an add-on web fragment, and will be responsible for extracting a 
parameter value to be provided to that web fragment. Declare your context parameter mapper in your atlassian-plugin.xml, e.g.

```
<connect-context-parameter-mapper key="my-context-parameter-mapper" class="com.atlassian.plugin.MyContextParameterMapper"/>
```