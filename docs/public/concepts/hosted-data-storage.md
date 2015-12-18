# Hosted data storage

Add-ons can store data in the form of entity properties in the host application. Properties are key-value pairs where the 
key is a string used to identify the property in all operations, and the value a JSON blob. Each host application allows 
properties to be stored on different types of entities, e.g. JIRA issues, Confluence pages, or the add-on itself. Your 
add-on will need to request the right [scopes](../scopes/scopes.html) to perform operations on entity properties.

Hosted data storage is useful to Atlassian Connect developers for the following reasons:

 * **Your add-on does not need to include a database to store data.**  
   Your add-on could be written as a set of static web pages using only HTML, CSS and Javascript, without any need for an application server.
 * **Imports and exports are handled by the host product.**  
   Since your data is stored with the host application it is included in the host applications backups. This means that the import process
   will restore your data automatically. With entity properties you never need to worry about your data being lost or disconnected from the customer.
 * **Conditions can be predicated on entity properties.**    
   Meaning that you can configure whether a web fragment will be shown based on a entity property; this is a much faster approach than relying upon remote conditions. This
   is because the host application must make a HTTP request to your add-on every time it evaluates a remote condition, blocking the display of your content until the request 
   finishes. With entity property conditions it is an order of magnitude faster to render the page and thus is a highly recommended approach.
 * **The products have access to your properties.**  
   In JIRA's case this means that you can write JQL queries based on issue entity properties. This enables your users to
   enjoy the power of [JQL on search data][1] that you have defined. In Confluence this means that you can use [CQL to search 
   for content][2].
   
Host properties are a powerful tool for Atlassian Connect developers. The following sections provide detailed explanations of how add-on properties,
entity properties and content properties may be used in your add-on.

## Table of contents

* [Add-on properties](#add-on-properties)
  * [Limitations of add-on properties](#add-on-properties-limitations)
  * [Supported Operations](#add-on-properties-supported-operations)
  * [Request example](#add-on-properties-request-example)
  * [Conditions based on add-on properties](#conditions-on-add-on-properties)
* [JIRA entity properties](#jira-entity-properties)
  * [Limitations of entity properties](#jira-entity-properties-limitations)
  * [Issue entity properties example](#jira-entity-properties-example) 
  * [Conditions on entity properties](#jira-entity-properties-conditions)
* [Confluence content properties](#confluence-content-properties)
  * [Limitations of Content Properties](#confluence-content-properties-limitations)
  * [Confluence content properties example](#confluence-content-properties-example)

## <a id="add-on-properties"></a>Add-on properties

Add-on properties are entity properties stored against the add-on itself. In this case the 'add-on' is considered
to be the storage container. However, add-on properties are still unique for each host application: the same add-on stored on
two different host applications wil not share the same add-on properties.

### <a id="add-on-properties-limitations"></a>Limitations of add-on properties

Add-on properties have the following limitations:

 * The properties for each add-on are sandboxed to the add-on. Only the add-on that writes the add-on properties can read those properties. 
   They cannot be shared or read by other add-ons.
 * Each add-on can create a maximum of 100 properties, each property value cannot be more than 32KB in size.
 * The value stored in each property must be in valid JSON format. (Valid JSON format is defined as anything that 
   [JSON.parse](https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/JSON/parse) can read)
 * Requests via [`AP.request`](../javascript/module-request.html) to store and receive add-on properties can only be made via a logged-in user.
 * There is no mechanism to handle concurrent edits by two users to the one add-on property. Whomever saves data last will win.
 
**Warning:** Add-on properties can be manipulated by a malicious authenticated user (e.g. by making REST calls through the developer console). For this reason:

 * Don't store user-specific data in add-on properties (particularly sensitive data).
 * Be defensive when retrieving add-on properties, and don't assume data consistency (arbitrary keys may be modified or deleted by users).

### <a id="add-on-properties-supported-operations"></a>Supported Operations

The following operations may be performed to manipulate add-on properties:

* [List properties](../rest-apis/index.html#get-addons-addonkey-properties)
* [Get property](../rest-apis/index.html#get-addons-addonkey-properties-propertykey)
* [Create or update property](../rest-apis/index.html#put-addons-addonkey-properties-propertykey)
* [Delete property](../rest-apis/index.html#delete-addons-addonkey-properties-propertykey)

### <a id="add-on-properties-request-example"></a>Request example

Add-on properties can be set like so:

    PUT /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property
    
    {"string":"string-value","number":5}

To request the value of the property we just set:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property?JSONValue=true
    
    {"key":"test-property","value":{"string":"string-value","number":5},"self":"..."}
    
Please note the use of the `JSONValue=true` query param. If you do not include that parameter then the response will be:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property
    
    {"key":"test-property","value":"{\"string\":\"string-value\",\"number\":5}","self":"..."}
     
Which returns the value as a JSON-escaped string. This is deprecated behaviour and will be going away in June 2016. Please 
always use the `JSONValue=true` query parameter.

Here is an example snippet that will show a pop-up with a JSON property named my-property-key for add-on with key my-add-on-key.

     AP.require(['request'], function(request) {
         request({
             url: '/rest/atlassian-connect/1/addons/my-add-on-key/properties/my-property-key?JSONValue=true',
             success: function(response) {
                 // Convert the string response to JSON
                 response = JSON.parse(response);
                 alert(response);
             },
             error: function(response) {
                 console.log("Error loading API (" + uri + ")");
                 console.log(arguments);
             },
             contentType: "application/JSON"
         });
     });

Apart from using [`AP.request`](../javascript/module-request.html), the same endpoints are accessible via a request signed with JWT.

### <a id="conditions-on-add-on-properties"></a>Conditions based on add-on properties

Add-on properties can be referenced in the `entity_property_equal_to` condition to decide whether or not to show a web fragment. 
For example, the following is a valid condition on the add-on property `activatedForUsers`:

    {
        condition: "entity_property_equal_to",
        params: {
            entity: "addon",
            propertyKey: "activatedForUsers",
            value: "true"
        }
    }
    
Only if that property is set to true against the add-on will the condition allow the web fragment to show. Thus you can use this to 
decide whether or not to show web fragments based on data that you have stored in add-on properties. This is very useful 
when you have host application wide configuration that you wish to rely upon.

 [1]: https://developer.atlassian.com/jiradev/jira-platform/building-jira-add-ons/jira-entity-properties-overview#JIRAEntityPropertiesOverview-HowdoImakethepropertiesofanentitysearchable?
 [2]: https://developer.atlassian.com/confdev/confluence-rest-api/advanced-searching-using-cql

## <a id="jira-entity-properties"></a>JIRA entity properties

JIRA provides a mechanism to store key-value pair data against JIRA entities and these are known as *entity properties*. The JIRA entities
that you can store properties against are:

 * [Issues](https://docs.atlassian.com/jira/REST/latest/#api/2/issue/{issueIdOrKey}/properties-getProperty)
 * [Projects](https://docs.atlassian.com/jira/REST/latest/#api/2/project/{projectIdOrKey}/properties-getProperty)
 * [Users](https://docs.atlassian.com/jira/REST/latest/#api/2/user/properties-getProperty)
 * [Issue types](https://docs.atlassian.com/jira/REST/latest/#api/2/issuetype/{issueTypeId}/properties-getProperty)
 * [Comments](https://docs.atlassian.com/jira/REST/latest/#api/2/comment/{commentId}/properties-getProperty)
 * [Workflows](https://docs.atlassian.com/jira/REST/latest/#api/2/workflow-updateProperty)
 * [Dashboard items](https://docs.atlassian.com/jira/REST/latest/#api/2/dashboard/{dashboardId}/items/{itemId}/properties-getProperty)
 
### <a id="jira-entity-properties-limitations"></a>Limitations of entity properties

Entity properties have the following limitations: 

 * Entity properties can be modified by all users that have permission to edit the entity; they are not sandboxed on a per-user basis.
   This means that the same entity property can be edited by two different users.
 * Entity properties can be modified by all add-ons in the system and exist in a global namespace. It is recommended that you namespace
   the entity property keys for the properties that you wish to be specific to your add-on. This also means that you should
   avoid storing unencrypted sensitive data in entity properties.
 * There is no mechanism to handle concurrent edits by two users to the one add-on property. Whomever saves data last will win.
 * The scopes that your add-on requires to modify entity properties are different depending on the type of entity property that you wish to modify.
   For example, to delete an issue entity property you only need `DELETE` scope. However, to delete a project entity property you require 
   `PROJECT_ADMIN` scope.
 * The value stored in each property must be in valid JSON format. (Valid JSON format is defined as anything that 
   [JSON.parse](https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/JSON/parse) can read)
   
Keep these limitations in mind when you use entity properties.

### <a id="jira-entity-properties-example"></a>Issue entity properties example

To set an entity property on an issue (ET-1) you can make the following request:

    PUT /rest/api/2/issue/ET-1/properties/party-members
              
    {"party": { "attendees": ["antman", "batman", "catwoman", "deadpool"], "attendeeCount": 4 }}
    
Then to get that data back you could make the following request:

    GET /rest/api/2/issue/ET-1/properties/party-members
     
    {
      "key": "party-members",
      "value": {
        "party": {
          "attendees": [
            "antman",
            "batman",
            "catwoman",
            "deadpool"
          ],
          "attendeeCount": 4
        }
      }
    }
         
In this example an issue entity property with the key *party-members* has been set on the issue ET-1.

You could then use the *jiraEntityProperties* module to index these issue entity properties so that the data becomes
available in JQL searches. [Read the jiraEntityProperties documentation](../modules/jira/entity-property.html) for more details.

### <a id="jira-entity-properties-conditions"></a>Conditions on entity properties

Conditions on entity properties provide a major performance advantage over remote conditions; since the entity property is
local to the host application the condition evaluates rapidly instead of requiring an entire HTTP call to take place.

You can use the `entity_property_equal_to` condition to decide whether or not to show a web fragment based on the data
in an entity property. For example, if we had an issue entity property with the key `isSpecialUser` and a value of `true` 
(JSON boolean) then we could write the following conditon:

    {
        condition: "entity_property_equal_to",
        params: {
            entity: "issue",
            propertyKey: "isSpecialUser",
            value: "true"
        }
    }

It is important to note that the `params.value` field currently expects a string. Therefore you will need to convert your
JSON into a string before you can compare it for equality. For example, to check that the JSON string "special" was stored 
in `isSpecialUser` then the condition must be written like so:

    {
        condition: "entity_property_equal_to",
        params: {
            entity: "issue",
            propertyKey: "isSpecialUser",
            value: "\"special\""
        }
    }
    
Also, there is currently no way to get a nested value out of a JSON object stored in an entity property for the purposes
of comparison in a condition.

## <a id="confluence-content-properties"></a>Confluence content properties

[Content properties](https://developer.atlassian.com/confdev/confluence-rest-api/content-properties-in-the-rest-api) are a 
key-value storage associated with a piece of Confluence content, and are one of the forms of persistence available to you as 
an add-on developer. The Confluence content that you can store content properties against are:

 * [Pages and Blogs](https://docs.atlassian.com/atlassian-confluence/REST/latest/#d3e163)
 
### <a id="confluence-content-properties-limitations"></a>Limitations of Content Properties

Content properties have the following limitations:

 * You can store an unlimited number of content properties against a piece of content but each property can have no more than 
   32kB of JSON data stored in it.
 * Content properties can be modified by all add-ons in the system and exist in a global namespace. It is recommended that you namespace
   then entity property keys for the properties that you wish to be specific to your add-on. This also means that you should
   avoid storing unencrypted sensitive data in entity properties.
 * The value stored in each property must be in valid JSON format. (Valid JSON format is defined as anything that 
   [JSON.parse](https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/JSON/parse) can read)
 
It is important to note that Content properties are unique in that they provide a mechanism to handle concurrent edits. The 'version'
field in the request and response ensures that two requests cannot update the same version of the entity properties data. Attempting
to do so will result in a HTTP error.

### <a id="confluence-content-properties-example"></a>Confluence content properties example

If you wanted to create a content property called 'my-property' on a piece of Confluence content with the id 12345 then
you would make the following request:

    PUT /rest/api/content/12345/property/my-property
    
    { 
        "key": "my-property",
        "version": { "number": 1 },
        "value": {"party": { "attendees": ["alex", "betty", "charles", "davinda"], "attendeeCount": 4 }}
    }
    
The structure of the payload in this request is different to entity and add-on properties. The differences are:

 * You need to provide the key in the JSON data as well as the URI
 * You need to provide a version number with the data. That version number must be higher than the
   previous version number or '1' if it is a brand new piece of content.
 * The actual data you wish to store must still be a JSON blob but it is nested inside the 'value' field of the root JSON object.

To update that property in the future you would need to bump the version number, like so:

    PUT /rest/api/content/12345/property/my-property
        
    { 
        "key": "my-property",
        "version": { "number": 2 },
        "value": {"party": { "attendees": ["antman", "batman", "catwoman", "deadpool"], "attendeeCount": 4 }}
    }

Each of these PUT requests will return the same data as a GET request on this resource. A get
request on the 'my-property' content property will return the following result:

    GET /rest/api/content/98305/property/my-property
    
    {
      "id": "786433",
      "key": "my-property",
      "value": {
        "party": {
          "attendees": [
            "alex",
            "betty",
            "charles",
            "davinda"
          ],
          "attendeeCount": 4
        }
      },
      "version": {
        "when": "2015-12-08T12:13:01.878+11:00",
        "message": "",
        "number": 1,
        "minorEdit": false
      },
      "_links": {
        "base": "http://my-site.atlassian.net/confluence",
        "context": "/confluence",
        "self": "http://my-site.atlassian.net/confluence/rest/api/content/98305/property/my-property"
      },
      "_expandable": {
        "content": "/rest/api/content/98305"
      }
    }

You can also [use the confluenceContentProperties module](../modules/confluence/content-property.html) to extract data from 
your content properties and have it indexed and available for search in CQL.