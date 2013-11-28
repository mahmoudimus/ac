Post functions carry out any additional processing required *after* a JIRA workflow transition is executed, such as:

* updating an issue's fields
* adding a comment to an issue

Your add-on needs to declare the URL that JIRA will invoke with an HTTP POST after the transition is completed.
Each POST will include the issue and transition details and the configuration of the workflow function. It
will also include the authentication headers that allow the add-on to validate the authenticity of that request.

Often, the workflow post function will allow some degree of configuration of its behavior. As an example:
You may want to react to a state transition only if the issue has a particular label, and you want the project
administrator to configure that label. For this purpose, three additional (optional) URLs in the descriptor
allow you to declare the pages that will show:

* The read-only view or summary of the configuration
* The form that is shown when a workflow post function is first created
* The form that is shown when a workflow post function is edited

All URLs are relative to the base URL that is declared in the connect-container element of the descriptor.

Example workflow post function descriptor block

    "jiraWorkflowPostFunctions": [
	   {
        "name": {
            "i18n": "my.function",
            "value": "My  function"
        },
        "description": {
            "i18n": "my.function.desc",
            "value": "My function description"
        },
        "triggered": {
            "url": "/triggered"
        },
        "create": {
            "url": "/create"
        },
        "edit": {
            "url": "/edit"
        },
        "view": {
            "url": "/view"
        }
    }]

## Contents of the HTTP POST

To understand the type of content that is sent to the add-on after a state transition, you can use the webhook
inspector tool. The [Webhook Inspector](https://bitbucket.org/atlassianlabs/webhook-inspector) is a Connect add-on
that you can install in your development environment to inspect the content of event messages.

Here is an example POST body:

     {
         "configuration": {
             "value": "Configuration from the post function edit page"
         },
         "issue": {
             "fields": {
                 "assignee": {
                     "active": true,
                     "avatarUrls": {
                         "16x16": "http://issues.example.com/jira/secure/useravatar?size=xsmall&avatarId=10062",
                         "24x24": "http://issues.example.com/jira/secure/useravatar?size=small&avatarId=10062",
                         "32x32": "http://issues.example.com/jira/secure/useravatar?size=medium&avatarId=10062",
                         "48x48": "http://issues.example.com/jira/secure/useravatar?avatarId=10062"
                     },
                     "displayName": "A. D. Ministrator (Sysadmin)",
                     "emailAddress": "admin@example.com",
                     "name": "admin",
                     "self": "http://issues.example.com/jira/rest/api/2/user?username=admin"
                 },
                 "attachment": [],
                 "comment": {
                     "comments": [],
                     "maxResults": 0,
                     "startAt": 0,
                     "total": 0
                 },
                 "components": [],
                 "created": "2013-11-18T17:56:23.864+1100",
                 "description": null,
                 "duedate": null,
                 "environment": null,
                 "fixVersions": [],
                 "issuetype": {
                     "description": "A problem which impairs or prevents the functions of the product.",
                     "iconUrl": "http://issues.example.com/jira/images/icons/issuetypes/bug.png",
                     "id": "1",
                     "name": "Bug",
                     "self": "http://issues.example.com/jira/rest/api/2/issuetype/1",
                     "subtask": false
                 },
                 "labels": [],
                 "lastViewed": "2013-11-18T17:56:31.793+1100",
                 "priority": {
                     "iconUrl": "http://issues.example.com/jira/images/icons/priorities/major.png",
                     "id": "3",
                     "name": "Major",
                     "self": "http://issues.example.com/jira/rest/api/2/priority/3"
                 },
                 "project": {
                     "avatarUrls": {
                         "16x16": "http://issues.example.com/jira/secure/projectavatar?size=xsmall&pid=10000&avatarId=10011",
                         "24x24": "http://issues.example.com/jira/secure/projectavatar?size=small&pid=10000&avatarId=10011",
                         "32x32": "http://issues.example.com/jira/secure/projectavatar?size=medium&pid=10000&avatarId=10011",
                         "48x48": "http://issues.example.com/jira/secure/projectavatar?pid=10000&avatarId=10011"
                     },
                     "id": "10000",
                     "key": "TEST",
                     "name": "Test",
                     "self": "http://issues.example.com/jira/rest/api/2/project/10000"
                 },
                 "reporter": {
                     "active": true,
                     "avatarUrls": {
                         "16x16": "http://issues.example.com/jira/secure/useravatar?size=xsmall&avatarId=10062",
                         "24x24": "http://issues.example.com/jira/secure/useravatar?size=small&avatarId=10062",
                         "32x32": "http://issues.example.com/jira/secure/useravatar?size=medium&avatarId=10062",
                         "48x48": "http://issues.example.com/jira/secure/useravatar?avatarId=10062"
                     },
                     "displayName": "A. D. Ministrator (Sysadmin)",
                     "emailAddress": "admin@example.com",
                     "name": "admin",
                     "self": "http://issues.example.com/jira/rest/api/2/user?username=admin"
                 },
                 "resolution": {
                     "description": "A fix for this issue is checked into the tree and tested.",
                     "id": "1",
                     "name": "Fixed",
                     "self": "http://issues.example.com/jira/rest/api/2/resolution/1"
                 },
                 "resolutiondate": "2013-11-18T17:56:31.799+1100",
                 "status": {
                     "description": "The issue is open and ready for the assignee to start work on it.",
                     "iconUrl": "http://issues.example.com/jira/images/icons/statuses/open.png",
                     "id": "1",
                     "name": "Open",
                     "self": "http://issues.example.com/jira/rest/api/2/status/1"
                 },
                 "summary": "The issue summary",
                 "updated": "2013-11-18T17:56:23.864+1100",
                 "versions": [],
                 "votes": {
                     "hasVoted": false,
                     "self": "http://issues.example.com/jira/rest/api/2/issue/TEST-1/votes",
                     "votes": 0
                 },
                 "watches": {
                     "isWatching": true,
                     "self": "http://issues.example.com/jira/rest/api/2/issue/TEST-1/watchers",
                     "watchCount": 1
                 },
                 "workratio": -1
             },
             "id": "10000",
             "key": "TEST-1",
             "self": "http://issues.example.com/jira/issue/10000"
         },
         "transition": {
             "from_status": "Open",
             "to_status": "Resolved",
             "transitionId": 5,
             "transitionName": "Resolve Issue",
             "workflowId": 10000,
             "workflowName": "classic default workflow"
         }
     }
