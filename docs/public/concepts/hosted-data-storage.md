# Hosted data storage

What is the use case that we are solving with this?
What hosted data storage options are avaliable?
What are the advantages and disadvantages of hosted data storage?
What are add-on properties and how do they work?
What are entity properties and how do they work?
What are content properties and how do they work?

There are three ways in which you can store data against JIRA and Confluence: add-on properties, entity properties 
(jira only) or content properties (confluence only). We will refer to them collectively as *host propeties*. A host 
property is a key-value pair where the value is a blob of JSON data. Every host property is stored inside a container in 
the host application. That container might be an issue in JIRA, a page in confluence or even the add-on itself. Your add-on 
will need to request the right [Scopes](../scopes/scopes.html) to perform operations on host properties.

Hosted data storage is useful to Atlassian Connect developers for the following reasons:

 * **Your addon does not need to have a backend to store data.**  
   You can store your data in the host application itself meaning that your addon could be written using frontend only technologies (like Javascript / HTML / CSS).
 * **Imports and exports are handled by the host product.**  
   Since your data is stored with the host application it is included in the host applications backups. This means that the import process
   will restore your data automatically. With host properties you never need to worry about your data being lost or disconnected from the customer.
 * **Conditions can be predicated on host properties.**    
   Meaning that you can configure if a web fragment will be shown based on a host property; this is a must faster approach than relying upon remote conditions. This
   is because the host application must make a HTTP request to your add-on every time it evaluates a remote condition; blocking the display of your content until the request 
   finishes. With host property conditions it is an order of magnitude faster to render the page and thus is a highly recommended approach.
 * **The products have access to your properties.**  
   In JIRA's case this means that you can write JQL queries based on issue entity properties. This enables your users to
   enjoy the power of JQL on search data that you have defined. In Confluence this means that you can use [CQL to search 
   for content](https://developer.atlassian.com/confdev/confluence-rest-api/advanced-searching-using-cql).
   
Host properties are a powerful tool for Atlassian Connect developers. The following sections provide detailed explanations of how add-on properties,
entity properties and content properties may be used in your add-on.

## <a id="add-on-properties">Add-on properties

Add-on properties are host properties who store each property against the add-on itself. In this case the 'add-on' is considered
to be the storage container. However, add-on properties are still unique for each host application: the same add-on stored on
two different host applications wil not share the same add-on properties.

### <a id="add-on-properties-limitations"></a>Limitations of add-on properties

Add-on properties have the following limitations:

 * The properties for each add-on are sandboxed to the add-on. Only the add-on that writes the add-on properties can read those properties. 
   They cannot be shared or read by other add-ons.
 * Each addon can create a maximum of 100 properties, each property value cannot be more than 32KB in size.
 * The value stored in each property must be in valid JSON format.
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

To give you a very basic example you can set an add-on property like so:

    PUT /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property
    
    {"string":"string-value","number":5}

And if you were to request it again you would get the following response:

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
 
Entity properties can be used in Conditions: enabling the showing and hiding of webfragments based on the value of an entity property.
 
### <a id="jira-entity-properties-limitations"></a>Limitations of entity properties

Entity properties have the following limitations: 

 * Entity properties can be modified by all users that have permission to edit the entity; they are not sandboxed on a per-user basis.
   This means that the same entity property can be edited by two different users.
 * Entity properties can be modified by all addons in the system and exist in a global namespace. It is recommended that you namespace
   then entity property keys for the properties that you wish to be specific to your addon. This also means that you should
   avoid storing unencrypted sensitive data in entity properties.
 * There is no mechanism to handle concurrent edits by two users to the one add-on property. Whomever saves data last will win.
 * Entity properties can only be modified as a logged in user.
 * The scopes that your addon requires to modify entity properties different depending on the type of entity property that you wish to modify.
   For example, to delete an issue entity property you only need `DELETE` scope however, to delete a project entity propecty you require 
   `PROJECT_ADMIN` scope.
 * The value stored in each property must be in valid JSON format.
   
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
available in JQL searches. [Read the jiraEntityProperties documentation](modules/jira/entity-property.html) for more details.

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

It is important to note that the `params.value` field currently expects a string. If you have a more complicated JSON object
that you wish to compare against the stored data in your entity property then you will need to convert your JSON into a string that
you compare for equality. To give you an example, if the true JSON boolean was actually the JSON string "special" then you should 
write the condition like so:

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

## <a id="confuence-content-properties"></a>Confluence content properties

[Content properties](https://developer.atlassian.com/confdev/confluence-rest-api/content-properties-in-the-rest-api) are a 
key-value storage associated with a piece of Confluence content, and are one of the forms of persistence available to you as 
an add-on developer. The Confluence content that you can store content properties against are:

 * Pages
 * Blog Posts
 
### <a id="confuence-content-properties-limitations"></a>Limitations of Content Properties

Content properties have the following limitations:

 * You can store an unlimited number of content properties against a piece of content but each property can have no more than 
   32kB of JSON data stored in it.
 * Content properties can be modified by all addons in the system and exist in a global namespace. It is recommended that you namespace
   then entity property keys for the properties that you wish to be specific to your addon. This also means that you should
   avoid storing unencrypted sensitive data in entity properties.
 * Content properties can only be modified as a logged in user.
 * The value stored in each property must be in valid JSON format.
 
It is important to note that Content properties are unique in that they provide a mechanism to handle concurrent edits. The 'version'
field in the request and response ensures that two requests cannot update the same version of the entity properties data. Attempting
to do so will result in a HTTP error.

### <a id="confuence-content-properties-example"></a>Confluence content properties example

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
        
These examples show how you can get and set content properties on your confluence content.

## <a id="conditions-on-host-properties"></a>Conditions based on host properties

Add-on properties can be referenced in the `entity_property_equal_to` condition to decide whether or not to show a web fragment. 
For example, the following is a valid condition on the addon property `activatedForUsers`:

    {
        condition: "entity_property_equal_to",
        params: {
            entity: "addon",
            propertyKey: "activatedForUsers",
            value: "true"
        }
    }
    
Only if that property is set to true against the addon will the condition allow the web fragment to show. Thus you can use this to 
decide wether or not to show web fragments based on data that you have stored in add-on properties. This is very useful 
when you have host application wide configuration that you wish to rely upon.