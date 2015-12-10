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
 * administrator to configure that label. For this purpose, three additional URLs in the descriptor
 * allow you to declare the pages that will show:
 *
 * * The form that is shown when a workflow post function is first created
 * * The form that is shown when a workflow post function is edited
 * * The read-only view or summary of the configuration
 *
 *All URLs are relative to the base URL that is declared in the connect-container element of the descriptor.
 *
 *#### Creating and editing a Post Function
 *
 *The create and edit urls will need to present a form with relevant configuration for the post function. In order to
 * persist this information with JIRA, the page needs to include a snippet of Javascript to facilitate saving this data.
 *
 *      AP.require(["jira"], function(jira) {
 *          // When the configuration is saved, this method is called. Return the values for your input elements.
 *          jira.WorkflowConfiguration.onSave(function() {
 *              var config = {
 *                  "key": "val"
 *              };
 *              return JSON.stringify(config);
 *          });
 *
 *          // Validate any appropriate input and return true/false
 *          jira.WorkflowConfiguration.onSaveValidation(function() {
 *              return true;
 *          });
 *      });
 *
 * For more information, see the [javascript API](../../javascript/WorkflowConfiguration.html).
 *
 *
 *#### Example
 *
 * For a full add-on example, see the [workflow post function example add-on](https://bitbucket.org/atlassianlabs/atlassian-connect-jira-workflow-post-function-example).
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#POST_FUNCTION_EXAMPLE}
 * @schemaTitle Workflow Post Function
 * @since 1.0
 */
@SchemaDefinition("workflowPostFunction")
public class WorkflowPostFunctionModuleBean extends RequiredKeyBean
{
    /**
     * The description of the workflow post function. This will be presented to the user when they add a new post
     * function to a JIRA workflow.
     */
    private I18nProperty description;

    /**
     * The relative URL to the add-on page that shows the read-only configuration or summary of the workflow post
     * function.
     *
     * The view URL can contain the following context parameters:
     *
     * - `postFunction.id`: The unique identifier of the post function
     * - `postFunction.config`: The configuration value saved to JIRA after calling `WorkflowConfiguration.onSave`
     */
    private UrlBean view;

    /**
     * The relative URL to the add-on page that allows to configure the workflow post function once it exists.
     *
     * The edit URL can contain the following context parameters:
     *
     * - `postFunction.id`: The unique identifier of the post function
     * - `postFunction.config`: The configuration value saved to JIRA after calling `WorkflowConfiguration.onSave`
     */
    private UrlBean edit;

    /**
     * The relative URL to the add-on page that allows to configure the workflow post function on creation.
     */
    private UrlBean create;

    /**
     * The relative URL to the add-on resource that will receive the HTTP POST after a workflow transition. It will also
     * include the authentication headers that allow the add-on to validate the authenticity of the request.
     *
     *#### Contents of the HTTP POST
     *
     *To understand the type of content that is sent to the add-on after a state transition, you can use the webhook
     * inspector tool. The [Webhook Inspector](https://bitbucket.org/atlassianlabs/webhook-inspector) is a Connect add-on
     * that you can install in your development environment to inspect the content of event messages.
     *
     *Here is an example POST body. For brevity, some fields have been removed or truncated.
     *
     *    {
     *        "configuration": {
     *            "value": "Configuration from the post function edit page"
     *        },
     *        "issue": {
     *            "fields": {
     *                "assignee": { },
     *                "attachment": [],
     *                "comment": { },
     *                "components": [],
     *                "created": "2013-11-18T17:56:23.864+1100",
     *                "description": null,
     *                "duedate": null,
     *                "environment": null,
     *                "fixVersions": [],
     *                "issuetype": { },
     *                "labels": [],
     *                "lastViewed": "2013-11-18T17:56:31.793+1100",
     *                "priority": { },
     *                "project": {
     *                    "avatarUrls": { },
     *                    "id": "10000",
     *                    "key": "TEST",
     *                    "name": "Test"
     *                },
     *                "reporter": { },
     *                "resolution": { },
     *                "resolutiondate": "2013-11-18T17:56:31.799+1100",
     *                "status": { },
     *                "summary": "The issue summary",
     *                "updated": "2013-11-18T17:56:23.864+1100",
     *                "versions": [],
     *                "votes": { },
     *                "watches": { },
     *                "workratio": -1
     *            },
     *            "id": "10000",
     *            "key": "TEST-1",
     *            "self": "http://issues.example.com/jira/issue/10000"
     *        },
     *        "transition": {
     *            "from_status": "Open",
     *            "to_status": "Resolved",
     *            "transitionId": 5,
     *            "transitionName": "Resolve Issue",
     *            "workflowId": 10000,
     *            "workflowName": "classic default workflow"
     *        }
     *    }
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
