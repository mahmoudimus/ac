package com.atlassian.plugin.connect.modules.beans;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.builder.WorkflowPostFunctionModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Post functions carry out any additional processing required *after* a JIRA workflow transition is executed, such as:
 *
 * * updating an issue's fields
 * * adding a comment to an issue
 *
 *Your add-on needs to declare the URL that JIRA will invoke with an HTTP POST after the transition is completed.
 * Each POST will include the issue and transition details and the configuration of the workflow function. It
 * will also include the authentication headers that allow the add-on to validate the authenticity of that request.
 *
 *Often, the workflow post function will allow some degree of configuration of its behavior. As an example:
 * You may want to react to a state transition only if the issue has a particular label, and you want the project
 * administrator to configure that label. For this purpose, three additional (optional) URLs in the descriptor
 * allow you to declare the pages that will show:
 *
 * * The read-only view or summary of the configuration
 * * The form that is shown when a workflow post function is first created
 * * The form that is shown when a workflow post function is edited
 *
 *All URLs are relative to the base URL that is declared in the connect-container element of the descriptor.
 *
 *## Contents of the HTTP POST
 *
 *To understand the type of content that is sent to the add-on after a state transition, you can use the webhook
 * inspector tool. The [Webhook Inspector](https://bitbucket.org/atlassianlabs/webhook-inspector) is a Connect add-on
 * that you can install in your development environment to inspect the content of event messages.
 *
 *Here is an example POST body:
 *
 *
 *      {
 *          "configuration": {
 *              "value": "Configuration from the post function edit page"
 *          },
 *          "issue": {
 *              "fields": {
 *                  "assignee": {
 *                      "active": true,
 *                      "avatarUrls": {
 *                          "16x16": "http://issues.example.com/jira/secure/useravatar?size=xsmall&avatarId=10062",
 *                          "24x24": "http://issues.example.com/jira/secure/useravatar?size=small&avatarId=10062",
 *                          "32x32": "http://issues.example.com/jira/secure/useravatar?size=medium&avatarId=10062",
 *                          "48x48": "http://issues.example.com/jira/secure/useravatar?avatarId=10062"
 *                      },
 *                      "displayName": "A. D. Ministrator (Sysadmin)",
 *                      "emailAddress": "admin@example.com",
 *                      "name": "admin",
 *                      "self": "http://issues.example.com/jira/rest/api/2/user?username=admin"
 *                  },
 *                  "attachment": [],
 *                  "comment": {
 *                      "comments": [],
 *                      "maxResults": 0,
 *                      "startAt": 0,
 *                      "total": 0
 *                  },
 *                  "components": [],
 *                  "created": "2013-11-18T17:56:23.864+1100",
 *                  "description": null,
 *                  "duedate": null,
 *                  "environment": null,
 *                  "fixVersions": [],
 *                  "issuetype": {
 *                      "description": "A problem which impairs or prevents the functions of the product.",
 *                      "iconUrl": "http://issues.example.com/jira/images/icons/issuetypes/bug.png",
 *                      "id": "1",
 *                      "name": "Bug",
 *                      "self": "http://issues.example.com/jira/rest/api/2/issuetype/1",
 *                      "subtask": false
 *                  },
 *                  "labels": [],
 *                  "lastViewed": "2013-11-18T17:56:31.793+1100",
 *                  "priority": {
 *                      "iconUrl": "http://issues.example.com/jira/images/icons/priorities/major.png",
 *                      "id": "3",
 *                      "name": "Major",
 *                      "self": "http://issues.example.com/jira/rest/api/2/priority/3"
 *                  },
 *                  "project": {
 *                      "avatarUrls": {
 *                          "16x16": "http://issues.example.com/jira/secure/projectavatar?size=xsmall&pid=10000&avatarId=10011",
 *                          "24x24": "http://issues.example.com/jira/secure/projectavatar?size=small&pid=10000&avatarId=10011",
 *                          "32x32": "http://issues.example.com/jira/secure/projectavatar?size=medium&pid=10000&avatarId=10011",
 *                          "48x48": "http://issues.example.com/jira/secure/projectavatar?pid=10000&avatarId=10011"
 *                      },
 *                      "id": "10000",
 *                      "key": "TEST",
 *                      "name": "Test",
 *                      "self": "http://issues.example.com/jira/rest/api/2/project/10000"
 *                  },
 *                  "reporter": {
 *                      "active": true,
 *                      "avatarUrls": {
 *                          "16x16": "http://issues.example.com/jira/secure/useravatar?size=xsmall&avatarId=10062",
 *                          "24x24": "http://issues.example.com/jira/secure/useravatar?size=small&avatarId=10062",
 *                          "32x32": "http://issues.example.com/jira/secure/useravatar?size=medium&avatarId=10062",
 *                          "48x48": "http://issues.example.com/jira/secure/useravatar?avatarId=10062"
 *                      },
 *                      "displayName": "A. D. Ministrator (Sysadmin)",
 *                      "emailAddress": "admin@example.com",
 *                      "name": "admin",
 *                      "self": "http://issues.example.com/jira/rest/api/2/user?username=admin"
 *                  },
 *                  "resolution": {
 *                      "description": "A fix for this issue is checked into the tree and tested.",
 *                      "id": "1",
 *                      "name": "Fixed",
 *                      "self": "http://issues.example.com/jira/rest/api/2/resolution/1"
 *                  },
 *                  "resolutiondate": "2013-11-18T17:56:31.799+1100",
 *                  "status": {
 *                      "description": "The issue is open and ready for the assignee to start work on it.",
 *                      "iconUrl": "http://issues.example.com/jira/images/icons/statuses/open.png",
 *                      "id": "1",
 *                      "name": "Open",
 *                      "self": "http://issues.example.com/jira/rest/api/2/status/1"
 *                  },
 *                  "summary": "The issue summary",
 *                  "updated": "2013-11-18T17:56:23.864+1100",
 *                  "versions": [],
 *                  "votes": {
 *                      "hasVoted": false,
 *                      "self": "http://issues.example.com/jira/rest/api/2/issue/TEST-1/votes",
 *                      "votes": 0
 *                  },
 *                  "watches": {
 *                      "isWatching": true,
 *                      "self": "http://issues.example.com/jira/rest/api/2/issue/TEST-1/watchers",
 *                      "watchCount": 1
 *                  },
 *                  "workratio": -1
 *              },
 *              "id": "10000",
 *              "key": "TEST-1",
 *              "self": "http://issues.example.com/jira/issue/10000"
 *          },
 *          "transition": {
 *              "from_status": "Open",
 *              "to_status": "Resolved",
 *              "transitionId": 5,
 *              "transitionName": "Resolve Issue",
 *              "workflowId": 10000,
 *              "workflowName": "classic default workflow"
 *          }
 *      }
 *
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#POST_FUNCTION_EXAMPLE}
 * @schemaTitle Workflow Post Function
 * @since 1.0
 */
@SchemaDefinition("workflowPostFunction")
public class WorkflowPostFunctionModuleBean extends RequiredKeyBean
{
    /**
     * The description of the add-on's functionality that will show up in the *Manage add-ons* page.
     */
    private I18nProperty description;

    /**
     * The relative URL to the add-on page that shows the read-only configuration or summary of the workflow post
     * function.
     */
    private UrlBean view;

    /**
     * The relative URL to the add-on page that allows to configure the workflow post function once it exists.
     */
    private UrlBean edit;

    /**
     * The relative URL to the add-on page that allows to configure the workflow post function on creation.
     */
    private UrlBean create;

    /**
     * The relative URL to the add-on resource that will receive the HTTP POST after a workflow transition.
     */
    @Required
    private UrlBean triggered;

    public WorkflowPostFunctionModuleBean()
    {
        this.description = new I18nProperty("", "");
        this.view = null;
        this.edit = null;
        this.create = null;
        this.triggered = null;
    }

    public WorkflowPostFunctionModuleBean(WorkflowPostFunctionModuleBeanBuilder builder)
    {
        super(builder);

        if (null == description)
        {
            this.description = new I18nProperty("", "");
        }
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public UrlBean getView()
    {
        return view;
    }

    public boolean hasView()
    {
        return null != view;
    }

    public UrlBean getEdit()
    {
        return edit;
    }

    public boolean hasEdit()
    {
        return null != edit;
    }

    public UrlBean getCreate()
    {
        return create;
    }

    public boolean hasCreate()
    {
        return null != create;
    }

    public UrlBean getTriggered()
    {
        return triggered;
    }

    public boolean hasTriggered()
    {
        return null != triggered;
    }

    public static WorkflowPostFunctionModuleBeanBuilder newWorkflowPostFunctionBean()
    {
        return new WorkflowPostFunctionModuleBeanBuilder();
    }

    public static WorkflowPostFunctionModuleBeanBuilder newWorkflowPostFunctionBean(WorkflowPostFunctionModuleBean defaultBean)
    {
        return new WorkflowPostFunctionModuleBeanBuilder(defaultBean);
    }

    @Override
    public boolean equals(Object otherObj)
    {
        if (otherObj == this)
        {
            return true;
        }

        if (!(otherObj instanceof WorkflowPostFunctionModuleBean && super.equals(otherObj)))
        {
            return false;
        }

        WorkflowPostFunctionModuleBean other = (WorkflowPostFunctionModuleBean) otherObj;

        return new EqualsBuilder()
                .append(description, other.description)
                .append(view, other.view)
                .append(edit, other.edit)
                .append(create, other.create)
                .append(triggered, other.triggered)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(13, 61)
                .appendSuper(super.hashCode())
                .append(description)
                .append(view)
                .append(edit)
                .append(create)
                .append(triggered)
                .build();
    }
}
