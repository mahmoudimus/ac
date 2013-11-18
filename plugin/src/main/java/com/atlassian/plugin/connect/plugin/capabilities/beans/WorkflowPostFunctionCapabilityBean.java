package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.WorkflowPostFunctionCapabilityBeanBuilder;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;


/**
 * Post functions carry out any additional processing required *after* a JIRA workflow transition is executed, such as:
 *
 * * updating an issue's fields
 * * adding a comment to an issue
 *
 * To be notified of the workflow transition, your add-on needs to declare the URL that JIRA will invoke with an
 * HTTP POST after the transition is completed. Each POST will include the issue and transition details, and it will
 * also include the authentication headers that allow the add-on to validate the authenticity of that request.
 *
 * Often, the workflow post function will allow some degree of configuration of its behavior. As an example:
 * You may want to react to a state transition only if the issue has a particular label, and you want the project
 * administrator to configure that label. For this purpose, three additional (optional) URLs in the descriptor
 * allow you to declare the pages that will show:
 *
 * * The read-only view or summary of the configuration
 * * The form that is shown when a workflow post function is first created
 * * The form that is shown when a workflow post function is edited
 *
 * All URLs are relative to the base URL that is declared in the connect-container element of the descriptor.
 *
 * Example workflow post function descriptor block
 * -----------------------------------------------
 *
 *     "workflowPostFunctions": [
 * 	   {
 *         "name": {
 *             "i18n": "my.function",
 *             "value": "My  function"
 *         },
 *         "description": {
 *             "i18n": "my.function.desc",
 *             "value": "My function description"
 *         },
 *         "triggered": {
 *             "url": "/triggered"
 *         },
 *         "create": {
 *             "url": "/create"
 *         },
 *         "edit": {
 *             "url": "/edit"
 *         },
 *         "view": {
 *             "url": "/view"
 *         }
 *     }]
 *
 * Contents of the HTTP POST
 * -------------------------
 * To understand the type of content that is sent to the add-on after a state transition, you can use the webhook
 * inspector tool. The [Webhook Inspector](https://bitbucket.org/atlassianlabs/webhook-inspector) is a Connect add-on
 * that you can install in your development environment to inspect the content of event messages.
 */

public class WorkflowPostFunctionCapabilityBean extends NameToKeyBean
{
    /**
     * The description of the add-on's functionality that will show up in the *Manage add-ons* page.
     */
    private I18nProperty description;

    /**
     * The relative URL to the add-on page that shows the read-only configuration or summary of the workflow post function.
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
    private UrlBean triggered;

    public WorkflowPostFunctionCapabilityBean()
    {
        this.description = new I18nProperty("", "");
        this.view = null;
        this.edit = null;
        this.create = null;
        this.triggered = null;
    }

    public WorkflowPostFunctionCapabilityBean(WorkflowPostFunctionCapabilityBeanBuilder builder)
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

    public static WorkflowPostFunctionCapabilityBeanBuilder newWorkflowPostFunctionBean()
    {
        return new WorkflowPostFunctionCapabilityBeanBuilder();
    }

    public static WorkflowPostFunctionCapabilityBeanBuilder newWorkflowPostFunctionBean(WorkflowPostFunctionCapabilityBean defaultBean)
    {
        return new WorkflowPostFunctionCapabilityBeanBuilder(defaultBean);
    }
}
