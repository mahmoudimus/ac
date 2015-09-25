# Conditions

A condition specifies requirements that must be met for a user interface element exposed by an add-on to be displayed
to a user. Typical use cases include requiring a user to be logged in or to have specific permissions.

Various types of modules accept conditions, including
[Pages](../modules/common/page.html), [Web Panels](../modules/common/web-panel.html) and [Web Items](../modules/common/web-item.html).
To see whether a certain module accepts conditions, see its specific module documentation page.

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
* [Boolean operations](#boolean-operations)
* [Remote conditions](#remote)
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

For example, an add-on could let administrators activate functionality per JIRA project, storing a boolean property
`isEnabled` on each project using the product's REST API, and then use
the `entity_property_equal_to` to test for it with the following module definition.

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
                            "propertyKey": "isEnabled",
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

A remote condition is a condition which is implemented as an add-on resource. Upon evaluation, the Atlassian application
issues a request to the remote resource and expects a response which specifies whether to show or hide the module feature.

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

The add-on can pass parameters to the remote condition as URL query parameters. Remote condition has request
authentication information passed through as a header, rather than as a query string parameter.

If there is an error communicating with the remote resource (for example, the request timeout period of 10 seconds elapses with no response),
then the failure will be logged and the condition will be evaluated as `false`.

## <a name="product-specific-conditions"></a>Appendix: List of predefined conditions

Each product defines a set of conditions relevant to its domain.

### <a name "common-conditions"></a>Common

* [`entity_property_equal_to`](#entity-property)
* `feature_flag`
* `user_is_admin`
* `user_is_logged_in`
* `user_is_sysadmin`

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
* `target_user_can_set_status`
* `target_user_has_personal_blog`
* `target_user_has_personal_space`
* `threaded_comments`
* `tiny_url_supported`
* `user_can_create_personal_space`
* `user_can_update_user_status`
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

#### <a name="jira-condition-parameters"></a>Condition parameter mappings

The following table shows the condition parameters available for `has_issue_permission` and `has_project_permission`
in Atlassian Connect module declarations and how they map to the permissions described in the
[Permissions](https://docs.atlassian.com/jira/latest/com/atlassian/jira/security/Permissions.html) class documentation.<br><br>

<table summary="JIRA condition parameter mappings">
    <thead>
        <tr>
        	<th>JIRA condition parameters</th>
        	<th>Atlassian Connect equivalent</th>
        </tr>
    </thead>
    <tbody>
        <tr>
        	<td>ADMINISTER</td>
        	<td>admin</td>
        </tr>
        <tr>
        	<td>USE</td>
        	<td>use</td>
        </tr>
        <tr>
        	<td>SYSTEM_ADMIN</td>
        	<td>sysadmin</td>
        </tr>
        <tr>
        	<td>PROJECT_ADMIN</td>
        	<td>project</td>
        </tr>
        <tr>
        	<td>BROWSE</td>
        	<td>browse</td>
        </tr>
        <tr>
        	<td>CREATE_ISSUE</td>
        	<td>create</td>
        </tr>
        <tr>
        	<td>EDIT_ISSUE</td>
        	<td>edit</td>
        </tr>
        <tr>
        	<td>EDIT_ISSUE</td>
        	<td>update</td>
        </tr>
        <tr>
        	<td>SCHEDULE_ISSUE</td>
        	<td>scheduleissue</td>
        </tr>
        <tr>
        	<td>ASSIGNABLE_USER</td>
        	<td>assignable</td>
        </tr>
        <tr>
        	<td>ASSIGN_ISSUE</td>
        	<td>assign</td>
        </tr>
        <tr>
        	<td>RESOLVE_ISSUE</td>
        	<td>resolv</td>
        </tr>
        <tr>
        	<td>CLOSE_ISSUE</td>
        	<td>close</td>
        </tr>
        <tr>
        	<td>WORKLOG_EDIT_ALL</td>
        	<td>worklogeditall</td>
        </tr>
        <tr>
        	<td>WORKLOG_EDIT_OWN</td>
        	<td>worklogeditown</td>
        </tr>
        <tr>
        	<td>WORKLOG_DELETE_OWN</td>
        	<td>worklogdeleteown</td>
        </tr>
        <tr>
        	<td>WORKLOG_DELETE_ALL</td>
        	<td>worklogdeleteall</td>
        </tr>
        <tr>
        	<td>WORK_ISSUE</td>
        	<td>work</td>
        </tr>
        <tr>
        	<td>LINK_ISSUE</td>
        	<td>link</td>
        </tr>
        <tr>
        	<td>DELETE_ISSUE</td>
        	<td>delete</td>
        </tr>
        <tr>
        	<td>CREATE_SHARED_OBJECTS</td>
        	<td>sharefilters</td>
        </tr>
        <tr>
        	<td>MANAGE_GROUP_FILTER_SUBSCRIPTIONS</td>
        	<td>groupsubscriptions</td>
        </tr>
        <tr>
        	<td>MOVE_ISSUE</td>
        	<td>move</td>
        </tr>
        <tr>
        	<td>SET_ISSUE_SECURITY</td>
        	<td>setsecurity</td>
        </tr>
        <tr>
        	<td>USER_PICKER</td>
        	<td>pickusers</td>
        </tr>
        <tr>
        	<td>VIEW_VERSION_CONTROL</td>
        	<td>viewversioncontrol</td>
        </tr>
        <tr>
        	<td>MODIFY_REPORTER</td>
        	<td>modifyreporter</td>
        </tr>
        <tr>
        	<td>VIEW_VOTERS_AND_WATCHERS</td>
        	<td>viewvotersandwatchers</td>
        </tr>
        <tr>
        	<td>MANAGE_WATCHER_LIST</td>
        	<td>managewatcherlist</td>
        </tr>
        <tr>
        	<td>BULK_CHANGE</td>
        	<td>bulkchange</td>
        </tr>
        <tr>
        	<td>COMMENT_EDIT_ALL</td>
        	<td>commenteditall</td>
        </tr>
        <tr>
        	<td>COMMENT_EDIT_OWN</td>
        	<td>commenteditown</td>
        </tr>
        <tr>
        	<td>COMMENT_DELETE_OWN</td>
        	<td>commentdeleteown</td>
        </tr>
        <tr>
        	<td>COMMENT_DELETE_ALL</td>
        	<td>commentdeleteall</td>
        </tr>
        <tr>
        	<td>ATTACHMENT_DELETE_OWN</td>
        	<td>attachdeleteown</td>
        </tr>
        <tr>
        	<td>ATTACHMENT_DELETE_ALL</td>
        	<td>attachdeleteall</td>
        </tr>
        <tr>
        	<td>CREATE_ATTACHMENT</td>
        	<td>attach</td>
        </tr>
        <tr>
        	<td>COMMENT_ISSUE</td>
        	<td>comment</td>
        </tr>
        <tr>
        	<td>VIEW_WORKFLOW_READONLY</td>
        	<td>viewworkflowreadonly</td>
        </tr>
    </tbody>
</table>

