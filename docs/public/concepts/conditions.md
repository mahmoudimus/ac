# Conditions

A condition specifies requirements that must be met for a user interface element exposed by an add-on to be displayed
to a user. Typical use cases include requiring a user to be logged in or to have specific permissions.

Various types of modules accept conditions, including
[Pages](../modules/common/page.html), [Web Panels](../modules/common/web-panel.html) and [Web Items](../modules/common/web-item.html).
To see whether a certain module accepts conditions, see its specific module documentation page.

Conditions may also be used in [context parameters](../concepts/context-parameters.html#inline-conditions).

There are different classes of conditions. Most commonly used are [the predefined conditions provided by each host product](#static).
When further customization is needed, conditions can be specified [in terms of properties stored by the add-on in the host product](#entity-property).

These conditions operate on properties of the user at the browser, the entity being viewed (e.g. an issue or a blog post)
or its container (e.g. a project or a space), or the entire system.

Add-ons can also use conditions as building blocks of [boolean expressions](#boolean-operations) containing the
logical operations conjunction (AND), disjunction (OR), or negation.

Finally, add-ons with complex requirements can define the behavior of a condition [using a remote service endpoint](#remote).
As these remote service invocations have a negative impact the user experience, their use is discouraged.

## Table of contents

* [Predefined conditions](#static)
  * [Condition parameters](#static-condition-parameters)
  * [Entity property conditions](#entity-property)
  * [can_use_application condition](#can-use-application)
* [Boolean operations](#boolean-operations)
* [Remote conditions](#remote)
  * [Caching remote conditions](#remote-caching)
* [Appendix: List of predefined conditions](#product-specific-conditions)
  * [Confluence](#confluence-conditions)
  * [JIRA](#jira-conditions)
    * [Condition parameter mappings](#jira-condition-parameters)

## <a name="static"></a>Predefined conditions

A predefined condition is a condition which is exposed from the host Atlassian application.
See [the list of predefined conditions](#product-specific-conditions).

For example, a condition that will evaluate when only logged-in users view the page is specified by the following
module declaration.


```
{
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "user_is_logged_in"
                    }
                ]
            }
        ]
    }
}
```

### <a name="static-condition-parameters"></a>Condition parameters

Certain predefined conditions accept parameters.

For example, the `has_issue_permission` condition passes only for users who have the permission specified in the
ondition. The issue for which permissions are checked is the issue being viewed by the user at the browser.

```
{
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "has_issue_permission",
                        "params": {
                            "permission": "resolv"
                        }
                    }
                ]
            }
        ]
    }
}
```

## <a name="entity-property"></a>Entity property conditions

Add-ons that need to impose custom requirements on when user interface elements are displayed can use the
predefined `entity_property_equal_to` condition. This condition allows fast comparisons to be made against data stored
by the add-on in the host product.

The `entity_property_equal_to` condition can be used for properties of a set of entities specific to each host product.
See the documentation of [the product REST API's](../rest-apis/#product-apis) for information about how to manage
properties for each entity.

The condition requires three parameters to be specified. The referenced entity is defined by the context of the page
being viewed, e.g. for the JIRA issue `FOO-123`, properties of both the issue itself and of the project `FOO` can be
referenced.

* `entity` - the entity on which the property has been stored
  * Common: [`addon`](hosted-data-storage.html)
  * JIRA: `project`, `issue`, `user`, `issuetype`, `comment`, `dashboarditem`
* `propertyKey` - the key of the property to check
* `value` - the value to compare the property value against

There is also another optional field that you can specify called `objectName`; this field lets you select which part
of the entity property JSON value you wish to compare against. For example, if an entity property had the following value:
 
    {
        "one": {
            "ignored": "value",
            "two": true
        },
        "also", "ignored"
    }
    
Then you could set the `value` parameter to `true` and the `objectName` parameter to "one.two" and the 
`entity_property_equal_to` condition would evaluate to true.

For example, an add-on could let administrators activate functionality per JIRA project, storing an entity property
with the key `mySettings` and the field `isEnabled` on each project using the product's REST API. Then use
the `entity_property_equal_to` to test for it with the following module definition:

```
{
    "modules": {
        "webPanels": [
            {
                "location": "atl.jira.view.issue.right.context",
                "conditions": [
                    {
                        "condition": "entity_property_equal_to",
                        "params": {
                            "entity": "project",
                            "propertyKey": "mySettings",
                            "objectName": "isEnabled",
                            "value": "true"
                        }
                    }
                ]
            }
        ]
    }
}
```

Also, an add-on that allows users to associate data with a JIRA issue could store a boolean property `hasContent`
on that issue indicating that the issue has additional data, and then use this condition to control the display of a
web panel with additional information.

## <a name="can-use-application"></a>can_use_application condition

`can_use_application` condition checks whether the current user is allowed to use a specific application 
 (like JIRA Software or JIRA Service Desk). 
 
 The condition is true if and only if both of the the following statements are true:
 
 * the application is installed and enabled on the JIRA instance
 * the user is permitted to use the application according to the installed licence
 
The condition requires an `applicationKey` parameter, for example:
 
 ```
 {
     "modules": {
         "generalPages": [
             {
                 "conditions": [
                     {
                         "condition": "can_use_application",
                         "params": { 
                            "applicationKey": "jira-software"
                         }
                     }
                 ]
             }
         ]
     }
 }
 ```
 
Supported application keys are:

* jira-core
* jira-servicedesk
* jira-software

If an unrecognized application key is provided then the condition will simply evaluate to `false`.

## <a name="boolean-operations"></a>Boolean operations

The [Composite Condition](../modules/fragment/composite-condition.html) module fragment can be used wherever a condition
is expected. This allows the construction of boolean expressions aggregating multiple conditions.

For example, a condition that will evaluate when only anonymous users view the page is specified by the following
module declaration.


```
{
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "user_is_logged_in",
                        "invert": false
                    }
                ]
            }
        ]
    }
}
```

## <a name="remote"></a>Remote conditions

A remote condition specifies a remote resource that will be queried for the condition result. 
Upon evaluation, the Atlassian application issues a request to the remote resource and expects a response which 
specifies whether to show or hide the module feature.

**NOTE** If a module with a remote condition is included on a page, the user at the browser will not see the page fully loaded
until the remote condition has returned, and, if applicable, the module itself has loaded. Considering this
negative impact on the user experience, add-ons are discouraged from using remote conditions. Consider if the condition
result can be precalculated, stored as an entity propertiy in the appropriate context in the host application, and then
checked using an [Entity property condition](#entity-property).

Remote conditions are URLs and must start with either 'https' or '/', and return a 200 HTTP response code with
a JSON body containing the boolean ```shouldDisplay``` field.

    {
        "shouldDisplay": false
    }

The following module declaration exemplifies the use of a remote condition.

    {
        "modules": {
            "generalPages": [
                {
                    "conditions": [
                        {
                            "condition": "/condition/onlyBettyCondition"
                        }
                    ]
                }
            ]
        }
    }

The add-on can pass parameters to the remote condition as URL query parameters. Usually authentication information is passed
to resources via query parameters; however, for remote conditions authentication information is passed through as a header.

If there is an error communicating with the remote resource (for example, the request timeout period of 10 seconds elapses with no response),
then the failure will be logged and the condition will be evaluated as `false`.

### <a name="remote-caching"></a>Caching remote conditions

When the remote application requests a remote condition, it will cache the results of that request, if HTTP cache headers are set on the response.
You should make use of this feature to improve performance of your application and decrease the number of requests that are made
to your add-on. To show you how that is done we shall walk through a hypothetical example. Pretend that you have written a remote
condition that points to the url `/condition/true` such that this resource will always return the json response
`{ shouldDisplay: true }`. In order to make sure that it is cached your response should contain a Cache-Control header
that looks something like this:

    Cache-Control: max-age=60, must-revalidate

This will cause the remote condition to be cached for 60 seconds. But, under what situations would that cache be missed, if any?
When the request is actually made some extra query parameters will be added and the request URL will look something like this:

    /condition/true?tz=Australia%2FSydney&loc=en-US&user_id=admin&user_key=admin&xdm_e=http%3A%2F%2Flocalhost%3A2991&xdm_c=channel-condition&cp=%2Fjira&lic=none&cv=1.1.65
    
So, if we were to cache this request what would have to change about this request in order to create a cache miss? We separate
the query parameters that are likely to cause a cache miss from the ones that are not.

The query parameters likely to cause a cache miss:

 * If your timezone or locale changes then the cache will be missed. (`tz` / `loc`)
 * If this same condition is requested by a different user (or their username changes) then the cache will be missed. (`user_id` / `user_key`)
 * If your add-on changes from unlicensed to licensed, or vice-versa, that will result in a cache miss. (`lic`)
 * If the Atlassian Connect framework version is upgraded then your cache will be missed. (`cv`)
 
The query parameters that will never result in a cache miss:

 * If the url to your cloud instance changes then the cache will be missed. But changing the url of a cloud instance is not currently possible. (`xdm_e`)
 * The `xdm_c` variable will not change for conditions; never resulting in a cache miss.
 * The `cp` (context path) variable will not change for a running product: never resulting in a cache miss.
  
As you can see, you only really need to consider the first three points. When you cache the results of a remote condition the
only reason that the cache will be ignored if if a different user requests that condition or your licensed state changes. Essentially, 
remote conditions will only be cached on a per-user basis; even if that user is the 'anonymous' user.

## <a name="product-specific-conditions"></a>Appendix: List of predefined conditions

Each product defines a set of conditions relevant to its domain.

### <a name "common-conditions"></a>Common

* [`entity_property_equal_to`](#entity-property)
* `feature_flag`
* `user_is_admin`
* `user_is_logged_in`
* `user_is_sysadmin`
* `addon_is_licensed`

### <a name="confluence-conditions"></a>Confluence

* `active_theme`
* `can_edit_space_styles`
* `can_signup`
* `content_has_any_permissions_set`
* `create_content`
* `email_address_public`
* `favourite_page`
* `favourite_space`
* `following_target_user`
* `has_attachment`
* `has_blog_post`
* `has_page`
* `has_space`
* `has_template`
* `latest_version`
* `not_personal_space`
* `printable_version`
* `showing_page_attachments`
* `space_function_permission`
* `space_sidebar`
* `target_user_has_personal_blog`
* `target_user_has_personal_space`
* `threaded_comments`
* `tiny_url_supported`
* `user_can_create_personal_space`
* `user_can_use_confluence`
* `user_favouriting_target_user_personal_space`
* `user_has_personal_blog`
* `user_has_personal_space`
* `user_is_confluence_administrator`
* `user_logged_in_editable`
* `user_watching_page`
* `user_watching_space`
* `user_watching_space_for_content_type`
* `viewing_content`
* `viewing_own_profile`

### <a name="jira-conditions"></a>JIRA

* `can_attach_file_to_issue`
* `can_manage_attachments`
* `has_issue_permission`
* `has_project_permission`
* `has_selected_project`
* `has_sub_tasks_available`
* `has_voted_for_issue`
* `is_admin_mode`
* `is_issue_assigned_to_current_user`
* `is_issue_editable`
* `is_issue_reported_by_current_user`
* `is_issue_unresolved`
* `is_sub_task`
* `is_watching_issue`
* `linking_enabled`
* `sub_tasks_enabled`
* `time_tracking_enabled`
* `user_has_issue_history`
* `user_is_project_admin`
* `user_is_the_logged_in_user`
* `voting_enabled`
* `watching_enabled`
* [`can_use_application`](#can-use-application)

#### <a name="jira-condition-parameters"></a>JIRA permission keys


`has_issue_permission`, `has_project_permission` and `has_global_permission` require a key of the permission that 
will be checked for the current user. The first two conditions check project permissions and the last one checks global permissions. 
Below you will find all the built-in permission keys. 

Note that you may also provide any of your custom permission keys 
(defined in the [project](../modules/jira/project-permission.html) or [global](../modules/jira/global-permission.html) permission module). 
Permissions defined by add-ons need to be prefixed with an add-on key followed by two underscores and only then the custom permission key, 
for example: `your.add.on.key__yourPermissionKey`.

##### Project permission keys

* ADD_COMMENTS
* ADMINISTER_PROJECTS
* ASSIGN_ISSUES
* ASSIGNABLE_USER
* BROWSE_PROJECTS
* CLOSE_ISSUES
* CREATE_ATTACHMENTS
* CREATE_ISSUES
* DELETE_ALL_ATTACHMENTS
* DELETE_ALL_COMMENTS
* DELETE_ALL_WORKLOGS
* DELETE_ISSUES
* DELETE_OWN_ATTACHMENTS
* DELETE_OWN_COMMENTS
* DELETE_OWN_WORKLOGS
* EDIT_ALL_COMMENTS
* EDIT_ALL_WORKLOGS
* EDIT_ISSUES
* EDIT_OWN_COMMENTS
* EDIT_OWN_WORKLOGS
* LINK_ISSUES
* MANAGE_WATCHERS
* MODIFY_REPORTER
* MOVE_ISSUES
* RESOLVE_ISSUES
* SCHEDULE_ISSUES
* SET_ISSUE_SECURITY
* TRANSITION_ISSUES
* VIEW_DEV_TOOLS
* VIEW_READONLY_WORKFLOW
* VIEW_VOTERS_AND_WATCHERS
* WORK_ON_ISSUES


##### Global permission keys

* ADMINISTER
* SYSTEM_ADMIN
* USER_PICKER
* CREATE_SHARED_OBJECTS
* MANAGE_GROUP_FILTER_SUBSCRIPTIONS
* BULK_CHANGE
