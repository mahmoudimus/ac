# Conditions

 * [JIRA Conditions](#jiraconditions)
 * [Confluence Conditions](#confluenceconditions)

A condition specifies requirements that must be met for a user to access the features or UI exposed by a module. For
instance, the condition can require a user to be an administrator, have edit permissions, and apply other requirements
for access. If the condition is not met, the panel, page, or other UI element exposed by the add-on does not appear on
the page.

Various types of modules accept conditions, including `generalPages`, `adminPages`, and `webItems`. To see whether a certain
module accepts conditions, see their specific module documentation page.

## Remote Conditions

    {
        "name": "My Addon",
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

For a remote condition, the Atlassian application issues a request to the remote resource and expects a response which
specifies whether to show or hide the module feature.

    {
        "shouldDisplay": false
    }

The add-on can pass parameters to the remote condition as URL query parameters. Remote condition has request
authentication information passed through as a header, rather than as a query string parameter.


## Static conditions

A static condition is a condition which is exposed from the host Atlassian application.

For example, a condition that will evaluate when only anonymous users view the page is specified by the following
module declaration:

```
{
    "name": "My Addon",
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "user_is_logged_in",
                        "invert": true
                    }
                ]
            }
        ]
    }
}
```

### Condition parameters

Certain static conditions also accept parameters. For example:

* `has_issue_permission`
* `has_project_permission`

These conditions restrict access to the modules based upon user permission settings for the issue or project. Note that behind the scenes, the issue permission check simply checks the project context for the issue and conducts the permission check for the user against that project.

You can pass parameters to conditions as follows:

```
{
    "name": "My Addon",
    "modules": {
        "generalPages": [
            {
                "conditions": [
                    {
                        "condition": "has_issue_permission",
                        "invert": false,
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

In this case, the user must have not just access to the issue but resolve permissions specifically. The permissions applicable to Atlassian Connect JIRA add-on modules are equivalent to those applicable to JIRA Java plugin development, as described in the [JIRA Permissions class reference](https://docs.atlassian.com/jira/latest/com/atlassian/jira/security/Permissions.html) documentation. The following section describes the mapping of [JIRA permissions] to the permissions you can use in the Atlassian Connect add-on descriptor.


# JIRA condition parameter mappings
The following table shows the condition parameters available for `has_issue_permission` and `has_project_permission` in Atlassian Connect module declarations and how they map to the permissions described in the [Permissions](https://docs.atlassian.com/jira/latest/com/atlassian/jira/security/Permissions.html) class documentation.

<table>
    <thead>
        <tr><th>JIRA condition parameters</th><th>Atlassian Connect equivalent</th></tr>
    </thead>
    <tbody>
        <tr><td>ADMINISTER</td><td>admin</td></tr>
        <tr><td>USE</td><td>use</td></tr>
        <tr><td>SYSTEM_ADMIN</td><td>sysadmin</td></tr>
        <tr><td>PROJECT_ADMIN</td><td>project</td></tr>
        <tr><td>BROWSE</td><td>browse</td></tr>
        <tr><td>CREATE_ISSUE</td><td>create</td></tr>
        <tr><td>EDIT_ISSUE</td><td>edit</td></tr>
        <tr><td>EDIT_ISSUE</td><td>update</td></tr>
        <tr><td>SCHEDULE_ISSUE</td><td>scheduleissue</td></tr>
        <tr><td>ASSIGNABLE_USER</td><td>assignable</td></tr>
        <tr><td>ASSIGN_ISSUE</td><td>assign</td></tr>
        <tr><td>RESOLVE_ISSUE</td><td>resolv</td></tr>
        <tr><td>CLOSE_ISSUE</td><td>close</td></tr>
        <tr><td>WORKLOG_EDIT_ALL</td><td>worklogeditall</td></tr>
        <tr><td>WORKLOG_EDIT_OWN</td><td>worklogeditown</td></tr>
        <tr><td>WORKLOG_DELETE_OWN</td><td>worklogdeleteown</td></tr>
        <tr><td>WORKLOG_DELETE_ALL</td><td>worklogdeleteall</td></tr>
        <tr><td>WORK_ISSUE</td><td>work</td></tr>
        <tr><td>LINK_ISSUE</td><td>link</td></tr>
        <tr><td>DELETE_ISSUE</td><td>delete</td></tr>
        <tr><td>CREATE_SHARED_OBJECTS</td><td>sharefilters</td></tr>
        <tr><td>MANAGE_GROUP_FILTER_SUBSCRIPTIONS</td><td>groupsubscriptions</td></tr>
        <tr><td>MOVE_ISSUE</td><td>move</td></tr>
        <tr><td>SET_ISSUE_SECURITY</td><td>setsecurity</td></tr>
        <tr><td>USER_PICKER</td><td>pickusers</td></tr>
        <tr><td>VIEW_VERSION_CONTROL</td><td>viewversioncontrol</td></tr>
        <tr><td>MODIFY_REPORTER</td><td>modifyreporter</td></tr>
        <tr><td>VIEW_VOTERS_AND_WATCHERS</td><td>viewvotersandwatchers</td></tr>
        <tr><td>MANAGE_WATCHER_LIST</td><td>managewatcherlist</td></tr>
        <tr><td>BULK_CHANGE</td><td>bulkchange</td></tr>
        <tr><td>COMMENT_EDIT_ALL</td><td>commenteditall</td></tr>
        <tr><td>COMMENT_EDIT_OWN</td><td>commenteditown</td></tr>
        <tr><td>COMMENT_DELETE_OWN</td><td>commentdeleteown</td></tr>
        <tr><td>COMMENT_DELETE_ALL</td><td>commentdeleteall</td></tr>
        <tr><td>ATTACHMENT_DELETE_OWN</td><td>attachdeleteown</td></tr>
        <tr><td>ATTACHMENT_DELETE_ALL</td><td>attachdeleteall</td></tr>
        <tr><td>CREATE_ATTACHMENT</td><td>attach</td></tr>
        <tr><td>COMMENT_ISSUE</td><td>comment</td></tr>
        <tr><td>VIEW_WORKFLOW_READONLY</td><td>viewworkflowreadonly</td></tr>
    </tbody>
</table>


<a name="jiraconditions" id="jiraconditions"></a>
# JIRA conditions
 * browse_users_permission
 * can_attach_file_to_issue
 * can_attach_screenshot_to_issue
 * can_convert_to_issue
 * can_convert_to_sub_task
 * can_create_shared_objects
 * can_manage_attachments
 * external_user_management_disabled
 * has_issue_permission
 * has_link_types_available
 * has_project_permission
 * has_selected_project
 * has_sub_tasks_available
 * has_voted_for_issue
 * is_admin_mode
 * is_field_hidden
 * is_issue_assigned_to_current_user
 * is_issue_editable
 * is_issue_unresolved
 * is_keyboard_shortcuts_enabled
 * is_sub_task
 * is_watching_issue
 * linking_enabled
 * not_version_context
 * smtp_mail_server_configured
 * sub_tasks_enabled
 * time_tracking_enabled
 * user_has_issue_history
 * user_is_admin
 * user_is_logged_in
 * user_is_project_admin
 * user_is_sysadmin
 * user_is_the_logged_in_user
 * voting_enabled
 * watching_enabled

<a name="confluenceconditions" id="confluenceconditions"></a>
# Confluence Conditions

 * active_theme
 * can_cluster
 * can_edit_space_styles
 * can_signup
 * content_has_any_permissions_set
 * create_content
 * email_address_public
 * favourite_page
 * favourite_space
 * following_target_user
 * has_attachment
 * has_blog_post
 * has_page
 * has_space
 * has_template
 * latest_version
 * not_personal_space
 * printable_version
 * showing_page_attachments
 * space_function_permission
 * target_user_can_set_status
 * target_user_has_personal_blog
 * target_user_has_personal_space
 * threaded_comments
 * tiny_url_supported
 * user_can_create_personal_space
 * user_can_update_user_status
 * user_can_use_confluence
 * user_favouriting_target_user_personal_space
 * user_has_personal_blog
 * user_has_personal_space
 * user_is_confluence_administrator
 * user_is_logged_in
 * user_is_sysadmin
 * user_logged_in_editable
 * user_watching_page
 * user_watching_space
 * user_watching_space_for_content_type
 * viewing_content
 * viewing_own_profile
 * writable_directory_exists


