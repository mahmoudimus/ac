# Hosted data storage

What is the use case that we are solving with this?
What hosted data storage options are avaliable?
What are the advantages and disadvantages of hosted data storage?
What are add-on properties and how do they work?
What are entity properties and how do they work?
What are content properties aond how do they work?

There are three ways in which you can store data against JIRA and Confluence: add-on properties, entity properties (jira only) or 
content properties (confluence only). We will refer to them collectively as *host propeties*. A host property is a key-value pair
where the value is a blob of JSON data. Your add-on will need to request the right Scopes to perform operations on host
properties.

Hosted data storage is useful to Atlassian Connect developers for the following reasons:

 * Your addon does not need to have a backend to store data.  
   You can store your data in the host application itself meaning that your addon could be written using frontend only technologies (like Javascript / HTML / CSS).
 * You don't need to worry about imports and exports.  
   Since your data is stored with the host application it is included in the host applications backups and the import process
   will restore your data for you. This means that you never need to worry about your data being lost or disconnected from the customer.
 * Conditions can be predicated on host properties    
   Meaning that you can configure if a web fragment will be shown based on an host property; this is a must faster approach than relying upon remote conditions. This
   is because a remote condition must make an entire HTTP call whereas a host property based condition is a local operation. It is an order of magnitude faster to render
   the page and a highle recommended approach.
 * The products have access to your properties  
   In JIRA's case this means that you can write JQL queries based on issue entity properties. This enables your users to enjoy the power of JQL on search data
   that you have defined.
   
Host properties give an Atlassian Connect developer a great many benefits. The following sections provide detailed explanations of how add-on properties,
entity properties and content properties may be used in your add-on.

## <a id="add-on-properties">Add-on properties

Add-on properties allow add-ons to store key-value pairs in the host application, either JIRA or Confluence, and retrieve them. 

### <a id="add-on-properties-limitations"></a>Limitations of add-on properties

Add-on properties have the following limitations:

 * Only the add-on that writes the add-on properties can read those properties. They cannot be shared or read by other add-ons.
 * Each addon can create a maximum of 100 properties, each property value cannot be more than 32KB in size.
 * The value stored in each property must be in valid JSON format.
 * Requests via [`AP.request`](../javascript/module-request.html) to store and receive add-on properties can only be made via a logged-in user.
 
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

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property?jsonValue=true
    
    {"key":"test-property","value":{"string":"string-value","number":5},"self":"..."}
    
Please note the use of the `jsonValue=true` query param. If you do not include that parameter then the response will be:

    GET /rest/atlassian-connect/1/addons/my-plugin-key/properties/my-property
    
    {"key":"test-property","value":"{\"string\":\"string-value\",\"number\":5}","self":"..."}
     
Which is deprecated behaviour and will be going away in June 2016. Please always use the `jsonValue=true` query parameter.

Here is an example snippet that will show a pop-up with a JSON property named my-property-key for add-on with key my-add-on-key.

     AP.require(['request'], function(request) {
         request({
             url: '/rest/atlassian-connect/1/addons/my-add-on-key/properties/my-property-key?jsonValue=true',
             success: function(response) {
                 // Convert the string response to JSON
                 response = JSON.parse(response);
                 alert(response);
             },
             error: function(response) {
                 console.log("Error loading API (" + uri + ")");
                 console.log(arguments);
             },
             contentType: "application/json"
         });
     });

Apart from using [`AP.request`](../javascript/module-request.html), the same endpoints are accessible via a request signed with JWT.

## JIRA entity properties

JIRA provides a mechanism to store key-value pair data against JIRA entities and these are known as *entity properties*. The JIRA entities
that you can store properties against are:

 * [Issues](https://docs.atlassian.com/jira/REST/latest/#api/2/issue/{issueIdOrKey}/properties-getProperty)
 * [Projects](https://docs.atlassian.com/jira/REST/latest/#api/2/project/{projectIdOrKey}/properties-getProperty)
 * [Users](https://docs.atlassian.com/jira/REST/latest/#api/2/user/properties-getProperty)
 * [Issue types](https://docs.atlassian.com/jira/REST/latest/#api/2/issuetype/{issueTypeId}/properties-getProperty)
 * [Comments](https://docs.atlassian.com/jira/REST/latest/#api/2/comment/{commentId}/properties-getProperty)
 * [Workflows](https://docs.atlassian.com/jira/REST/latest/#api/2/workflow-updateProperty)
 * [Dashboard items](https://docs.atlassian.com/jira/REST/latest/#api/2/dashboard/{dashboardId}/items/{itemId}/properties-getProperty)
 
### Limitations of entity properties

All users and add-ons in the system can see all entity properties that they have access to. For example, if user A has access to a JIRA issue but user B does 
not then A can see all of the entity properties on the issue and B will get a HTTP 403 when they attempt to query for any properties. This means that a malicious 
user can modify the state of entity properties for everybody. Don't put sensitive data in these entity properties.
This also means that you should namespace your entity properties to avoid conflicts with entity properties that other addons or users might create.

Please note that you can only get or modify entity properties as a logged in user.
 
### Issue entity properties example

To set an entity property on an issue you can make the following request:

    PUT /rest/api/2/issue/ET-1/properties/party-addon-properties
              
    {"party": { "attendees": ["alex", "betty", "charles", "davinda"], "attendeeCount": 4 }}
    
Then to get that data back you could make the following request:

    GET /rest/api/2/issue/ET-1/properties/party-addon-properties
     
    {
      "key": "party-addon-properties",
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
      }
    }
         
In this example an issue entity property with the key *party-addon-properties* has been set on the issue ET-1.

You could then use the *jiraEntityProperties* module to index these issue entity properties so that they became avaliable
in JQL searches. [Read the jiraEntityProperties documentation](modules/jira/entity-property.html) for more details.

### Conditions on entity properties

Conditions on entity properties provide a major performance advantage over remote conditions; since the entity property is
local to the host application the condition evaluates rapidly instead of requiring an entire HTTP call to take place.

You can use the `entity_property_equal_to` condition to decide whether or not to show a web fragment based on the data
in an entity property. For example, if we had an issue entity property with the key `isSpecialUser` and a value of `true` 
(json boolean) then we could write the following conditon:

    {
        condition: "entity_property_equal_to",
        params: {
            entity: "issue",
            propertyKey: "isSpecialUser",
            value: "true"
        }
    }

It is important to note that the `params.value` field currently expects a string. If you have a more complicated JSON object
that you wish to compare against stored in your entity property then you will need to convert your json into a string that
you compare for equality. To give you an example, if the true json boolean was actually the json string "special" then you should 
write the condition like so:

    {
        condition: "entity_property_equal_to",
        params: {
            entity: "issue",
            propertyKey: "isSpecialUser",
            value: "\"special\""
        }
    }
    
Also, there is currently no way to get a nested value out of a json object stored in an entity property for the purposes of comparison.

## Confluence content properties

TODO

## Conditions based on host properties

TODO